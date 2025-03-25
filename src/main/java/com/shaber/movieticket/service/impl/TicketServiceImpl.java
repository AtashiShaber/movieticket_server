package com.shaber.movieticket.service.impl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.OrderDto;
import com.shaber.movieticket.dto.TicketDto;
import com.shaber.movieticket.exception.OrderServiceException;
import com.shaber.movieticket.exception.TicketServiceException;
import com.shaber.movieticket.exception.UserServiceException;
import com.shaber.movieticket.mapper.OrderMapper;
import com.shaber.movieticket.mapper.TicketMapper;
import com.shaber.movieticket.mapper.UserMapper;
import com.shaber.movieticket.pojo.*;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.TicketService;
import com.shaber.movieticket.utils.JwtUtil;
import com.shaber.movieticket.utils.SnowflakeIdWorker;
import com.shaber.movieticket.vo.TicketAddVO;
import com.shaber.movieticket.vo.pagequery.TicketPageQueryVO;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TicketServiceImpl implements TicketService {
    @Autowired
    private TicketMapper ticketMapper;

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Transactional
    @Override
    public RV buildTicket(String authHeader, TicketAddVO ticketAddVO) {
        // 通过redis获取登录的uid
        String redisKey = authHeader.replace("user:","user:token:");
        String token = redisTemplate.opsForValue().get(redisKey);
        // 如果token不存在
        if (token == null) {
            return RV.noData(401,"用户登录信息不存在！", null);
        }
        Claims claims = jwtUtil.parseToken(token);
        String uid = claims.get("id", String.class).replaceAll("ID_", "");
        User user = userMapper.findUserByUid(uid);

        // 判断用户是否为空
        if (user == null) {
            return RV.noData(401,"用户信息不存在！", null);
        }

        // 首先查询该电影的该场次是否有重复
        if (ticketMapper.findTicketBySidTseat(ticketAddVO.getSid(),ticketAddVO.getTseat()) != null) {
            throw new TicketServiceException("该票信息已经存在了");
        }

        // 不存在则进行创建票务信息
        if (ticketMapper.buildTicket(String.valueOf(snowflakeIdWorker.nextId()),
                ticketAddVO.getSid(),ticketAddVO.getTseat(),-1) <= 0) {
            throw new TicketServiceException("购票出现异常！");
        }

        // 获取用于Ticket信息
        Ticket ticket = ticketMapper.getTicketBySidAndTseat(ticketAddVO.getSid(), ticketAddVO.getTseat());

        // 正常则添加订单信息
        if (orderMapper.buildOrder(String.valueOf(snowflakeIdWorker.nextId()),
                uid, LocalDateTime.now(), ticket.getTid(), ticketAddVO.getOprice(), 0) <= 0) {
            throw new OrderServiceException("订单创建失败！");
        }

        // 如果用户的个人资金大于目前售票金额，则正常进行后续操作，不然取消
        if (userMapper.findUserByUid(uid).getUmoney().compareTo(ticketAddVO.getOprice()) < 0 ){
            return RV.fail("用户资金不足！请及时充值");
        }

        // 进行扣款操作
        if (userMapper.pay(uid, ticketAddVO.getOprice()) <= 0){
            throw new UserServiceException("扣款失败！");
        }

        // 修改订单跟票的状态
        Order order = orderMapper.findOrderByTidUid(ticket.getTid(), uid);
        if (orderMapper.updateOrder(order.getOid(), 1) <= 0){
            throw new OrderServiceException("订单修改失败！");
        }
        if (ticketMapper.updateStatus(ticket.getTid(), 0) <= 0){
            throw new TicketServiceException("票状态修改失败！");
        }

        return RV.success("创建成功！");
    }

    @Transactional
    @Override
    public RV useTicket(String tid) {
        // 首先查询票的信息,判断票是否已被使用或者退款
        Ticket ticket = ticketMapper.findTicket(tid);
        if (ticket.getTstatus() != 0) {
            throw new TicketServiceException("无法使用此票，因为已经退款或者使用！");
        }

        // 没有问题并且未使用的情况才能使用票
        if (ticketMapper.updateStatus(tid, 1) <= 0) {
            throw new TicketServiceException("票使用异常！");
        }

        return RV.success("使用成功！");
    }

    @Override
    public RV<PageInfo<TicketDto>> listTicket(String authHeader, TicketPageQueryVO ticketPageQueryVO) {
        // 通过redis获取登录的uid
        String redisKey = authHeader.replace("user:","user:token:");
        String token = redisTemplate.opsForValue().get(redisKey);
        // 如果token不存在
        if (token == null) {
            return RV.noData(401,"用户登录信息不存在！", null);
        }
        Claims claims = jwtUtil.parseToken(token);
        String uid = claims.get("id", String.class).replaceAll("ID_", "");
        User user = userMapper.findUserByUid(uid);

        // 判断用户是否为空
        if (user == null) {
            return RV.noData(401,"用户信息不存在！", null);
        }
        // 进行分页操作
        PageHelper.startPage(ticketPageQueryVO.getPageNum(), ticketPageQueryVO.getPageSize());
        // 进行查询
        List<TicketDto> ticketDtos = ticketMapper.selectTickets(uid,
                                            ticketPageQueryVO.getMname(),
                                            ticketPageQueryVO.getCname(),
                                            ticketPageQueryVO.getSrname(),
                                            ticketPageQueryVO.getSday());

        PageInfo<TicketDto> data = new PageInfo<>(ticketDtos);

        return RV.success("查询完毕", data);
    }

    @Override
    public RV<List<Ticket>> listTicketBySid(String sid) {
        List<Ticket> tickets = ticketMapper.findTicketsBySid(sid);
        return RV.success("查询完毕！",tickets);
    }

    @Transactional
    @Override
    public RV autoUse(String authHeader) {
        // 通过redis获取登录的uid
        String redisKey = authHeader.replace("user:","user:token:");
        String token = redisTemplate.opsForValue().get(redisKey);
        // 如果token不存在
        if (token == null) {
            return RV.noData(401,"用户登录信息不存在！", null);
        }
        Claims claims = jwtUtil.parseToken(token);
        String uid = claims.get("id", String.class).replaceAll("ID_", "");
        User user = userMapper.findUserByUid(uid);

        // 判断用户是否为空
        if (user == null) {
            return RV.noData(401,"用户信息不存在！", null);
        }

        // 3. 获取需要处理的票务数据
        LocalDate currentDate = LocalDate.now();
        List<Ticket> tickets = ticketMapper.findTicketOver(uid, currentDate);

        // 4. 过滤需要自动使用的票
        List<String> validTids = tickets.stream()
                .filter(this::isExpiredTicket)
                .map(Ticket::getTid)
                .collect(Collectors.toList());

        // 5. 批量更新状态
        if (!validTids.isEmpty()) {
            int updated = ticketMapper.batchUpdateStatus(validTids, 1);
            log.info("自动使用票务成功更新{}条记录", updated);
        }

        return RV.success("自动使用处理完成");

    }

    /**
     * 判断票务是否过期需要自动使用
     *
     * @param ticket 票务对象
     * @return 是否需要自动使用
     */
    private boolean isExpiredTicket(Ticket ticket) {
        // 解析场次日期和时间
        String screeningDay = ticket.getSid();
        String[] timeRange = screeningDay.split("-");

        // 转换开始时间
        LocalTime startTime = LocalTime.parse(timeRange[0].trim());

        // 判断逻辑
        return LocalTime.now().isAfter(startTime);
    }
}
