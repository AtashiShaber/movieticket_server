package com.shaber.movieticket.service.impl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.TicketDto;
import com.shaber.movieticket.exception.TicketServiceException;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



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
                ticketAddVO.getSid(),ticketAddVO.getTseat(),ticketAddVO.getTstatus()) <= 0) {
            throw new TicketServiceException("购票出现异常！");
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
        PageInfo<TicketDto> data = ticketMapper.selectTickets(uid,
                                            ticketPageQueryVO.getMname(),
                                            ticketPageQueryVO.getCname(),
                                            ticketPageQueryVO.getSrname(),
                                            ticketPageQueryVO.getSday());

        return RV.success("查询完毕", null);
    }
}
