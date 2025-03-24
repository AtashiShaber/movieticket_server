package com.shaber.movieticket.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.OrderDto;
import com.shaber.movieticket.exception.OrderServiceException;
import com.shaber.movieticket.exception.UserServiceException;
import com.shaber.movieticket.mapper.OrderMapper;
import com.shaber.movieticket.mapper.TicketMapper;
import com.shaber.movieticket.mapper.UserMapper;
import com.shaber.movieticket.pojo.Order;
import com.shaber.movieticket.pojo.Ticket;
import com.shaber.movieticket.pojo.User;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.OrderService;
import com.shaber.movieticket.utils.JwtUtil;
import com.shaber.movieticket.utils.SnowflakeIdWorker;
import com.shaber.movieticket.vo.OrderAddVO;
import com.shaber.movieticket.vo.pagequery.PageQueryVO;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TicketMapper ticketMapper;

    @Override
    public RV<Integer> countOrdersToday() {
        return RV.success("统计完毕！",orderMapper.countOrderToday());
    }

    @Override
    public RV<BigDecimal> countOrdersTodaySales() {
        List<Order> orders = orderMapper.countOrdersToday();
        BigDecimal total = BigDecimal.ZERO;
        for (Order order : orders) {
            total = total.add(order.getOprice());
        }
        System.out.println(total);
        return RV.success("统计完毕！",total);
    }

    @Override
    public RV<Float> refundRate() {
        // 获取今日所有订单（未付款除外）的数量
        float countOrderTodayAllPaid = (float) orderMapper.countOrderTodayAllPaid();
        // 获取今日所有已支付订单的数量
        float countOrderToday = (float) orderMapper.countOrderToday();

        // 计算比率
        float refundRate = (countOrderTodayAllPaid - countOrderToday) / countOrderToday * 100;

        return RV.success("获取成功！",refundRate);
    }

    @Transactional
    @Override
    public RV refund(String authHeader, String oid) {
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

        // 判断订单的状态是否为已支付
        Order order = orderMapper.getOrderByOid(oid);
        if (order.getOstatus() != 1) {
            throw new OrderServiceException("订单状态不是已支付！");
        }

        // 判断票是否为未使用
        Ticket ticket = ticketMapper.findTicket(order.getTid());
        if (ticket.getTstatus() != 0) {
            throw new OrderServiceException("不支持退票，因为票的状态不是未使用！");
        }

        // 判断完成进行退票处理
        if (orderMapper.updateOrder(oid, 2) <= 0) {
            throw new OrderServiceException("订单修改状态失败！");
        }
        if (ticketMapper.updateStatus(order.getTid(), 2) <= 0) {
            throw new OrderServiceException("票状态修改失败！");
        }
        if (userMapper.backMoney(order.getUid(), order.getOprice()) <= 0) {
            throw new OrderServiceException("退款失败！");
        }

        return RV.success("退款成功！");
    }

    @Transactional
    @Override
    public RV buildOrder(String authHeader, OrderAddVO orderAddVO) {
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

        // 如果没有问题，则进行创建订单
        if (orderMapper.buildOrder(String.valueOf(snowflakeIdWorker.nextId()), uid, LocalDateTime.now(),
                orderAddVO.getTid(), orderAddVO.getOprice(), orderAddVO.getOstatus()) <= 0) {
            throw new OrderServiceException("创建失败！");
        }

        return RV.success("创建完成！");
    }

    @Transactional
    @Override
    public RV pay(String authHeader, OrderAddVO orderAddVO) {
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

        // 通过uid来进行扣款与改变订单状态
        if (userMapper.pay(uid, orderAddVO.getOprice()) <= 0) {
            throw new UserServiceException("扣款失败！");
        }
        // 成功扣款则进行修改订单状态
        Order order = orderMapper.findOrderByTidUid(orderAddVO.getTid(), uid);
        // 如果订单创建失败但是继续运行到这一步，需要检测
        if (order == null) {
            throw new OrderServiceException("订单创建异常！");
        }
        if (orderMapper.updateOrder(order.getOid(), 1) <= 0) {
            throw new OrderServiceException("订单确认支付失败！");
        }

        return RV.success("订单确认支付成功！");
    }

    @Override
    public RV<PageInfo<OrderDto>> listOrder(String authHeader, PageQueryVO pageQueryVO) {
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

        // 若是成功登录则进行查询
        PageHelper.startPage(pageQueryVO.getPageNum(), pageQueryVO.getPageSize());
        List<OrderDto> orderDtos = orderMapper.listOrder(uid);
        PageInfo<OrderDto> data = new PageInfo<>(orderDtos);

        return RV.success("查询完毕",data);
    }
}
