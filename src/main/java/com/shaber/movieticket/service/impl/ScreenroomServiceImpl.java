package com.shaber.movieticket.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.ScreenroomDto;
import com.shaber.movieticket.exception.ScreenRoomServiceException;
import com.shaber.movieticket.mapper.*;
import com.shaber.movieticket.pojo.*;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.ScreenroomService;
import com.shaber.movieticket.utils.SnowflakeIdWorker;
import com.shaber.movieticket.vo.ScreenroomVO;
import com.shaber.movieticket.vo.pagequery.ScreenroomPageQueryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScreenroomServiceImpl implements ScreenroomService {
    @Autowired
    private ScreenRoomMapper screenRoomMapper;

    @Autowired
    private SnowflakeIdWorker snowflakeIdWorkerl;

    @Autowired
    private ScreeningMapper screeningMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TicketMapper ticketMapper;

    @Transactional
    @Override
    public RV addScreenroom(ScreenroomVO screenroomVO) {
        //首先判断是否存在重复的放映厅
        if (screenRoomMapper.findScreenroomsBySrnameCid(screenroomVO.getCid(),screenroomVO.getSrname()) != null){
            throw new ScreenRoomServiceException("该影厅名重复！");
        }

        //进行添加操作
        Screenroom screenroom = new Screenroom();
        BeanUtils.copyProperties(screenroomVO,screenroom);
        screenroom.setSrid(String.valueOf(snowflakeIdWorkerl.nextId()));
        if (screenRoomMapper.insertScreenRoom(screenroom) <= 0){
            throw new ScreenRoomServiceException("添加放映厅失败!");
        }

        return RV.success("添加成功！");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public RV deleteScreenroom(String srid) {
        // 1. 校验放映厅存在
        Screenroom screenroom = screenRoomMapper.getScreenRoom(srid);
        if (screenroom == null) {
            throw new ScreenRoomServiceException("放映厅不存在");
        }

        // 2. 批量处理关联数据
        processScreenroomData(srid);

        // 3. 删除放映厅
        if (screenRoomMapper.deleteScreenroom(srid) <= 0) {
            throw new ScreenRoomServiceException("放映厅删除失败");
        }

        return RV.success("删除成功");
    }

    private void processScreenroomData(String srid) {
        // 1. 批量获取场次数据
        List<Screening> screenings = screeningMapper.findScreeningsBySrid(srid);
        if (screenings.isEmpty()) return;

        // 2. 处理未来场次退款
        processFutureScreenings(screenings);

        // 3. 批量删除关联数据
        List<String> sids = screenings.stream()
                .map(Screening::getSid)
                .collect(Collectors.toList());

        deleteRelatedData(sids);
    }

    private void processFutureScreenings(List<Screening> screenings) {
        // 1. 过滤未来场次
        List<Screening> futureScreenings = screenings.stream()
                .filter(this::isFutureScreening)
                .collect(Collectors.toList());
        if (futureScreenings.isEmpty()) return;

        // 2. 批量处理票务退款
        List<String> futureSids = futureScreenings.stream()
                .map(Screening::getSid)
                .collect(Collectors.toList());
        List<Ticket> tickets = ticketMapper.findTicketsBySids(futureSids);

        processRefunds(tickets);
    }

    private boolean isFutureScreening(Screening screening) {
        LocalDate today = LocalDate.now();
        if (screening.getSday().isAfter(today)) return true;
        if (!screening.getSday().equals(today)) return false;

        try {
            String currentTime = LocalTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
            String startTime = screening.getStime().split("-")[0];
            return startTime.compareTo(currentTime) > 0;
        } catch (Exception e) {
            log.warn("异常时间格式 sid={} stime={}",
                    screening.getSid(), screening.getStime());
            return false;
        }
    }

    private void processRefunds(List<Ticket> tickets) {
        // 1. 过滤未使用票
        List<String> refundTids = tickets.stream()
                .filter(t -> t.getTstatus() == 0)
                .map(Ticket::getTid)
                .collect(Collectors.toList());
        if (refundTids.isEmpty()) return;

        // 2. 批量处理订单
        List<Order> orders = orderMapper.findOrdersByTids(refundTids);
        refundUsers(orders);

        // 3. 批量删除数据
        batchDeleteData(refundTids, orders);
    }

    private void refundUsers(List<Order> orders) {
        // 按用户聚合退款金额
        Map<String, BigDecimal> refundMap = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getUid,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Order::getOprice,
                                BigDecimal::add
                        )
                ));

        // 批量退款
        refundMap.forEach((uid, amount) -> {
            if (userMapper.addBalance(uid, amount) <= 0) {
                throw new ScreenRoomServiceException("用户退款失败: " + uid);
            }
        });
    }

    private void batchDeleteData(List<String> tids, List<Order> orders) {
        // 批量删除订单
        List<String> oids = orders.stream()
                .map(Order::getOid)
                .collect(Collectors.toList());
        if (!oids.isEmpty() && orderMapper.batchDelete(oids) != oids.size()) {
            throw new ScreenRoomServiceException("订单删除失败");
        }

        // 批量删除票
        if (ticketMapper.batchDelete(tids) != tids.size()) {
            throw new ScreenRoomServiceException("票删除失败");
        }
    }

    private void deleteRelatedData(List<String> sids) {
        // 批量删除历史票记录
        for (Ticket ticketsBySid : ticketMapper.findTicketsBySids(sids)) {
            orderMapper.deleteOrderByTid(ticketsBySid.getTid());
        }
        ticketMapper.batchDeleteBySids(sids);
        screeningMapper.batchDelete(sids);
        // 批量删除场次
        if (screeningMapper.batchDelete(sids) != sids.size()) {
            throw new ScreenRoomServiceException("场次删除失败");
        }
    }


    @Transactional
    @Override
    public RV updateScreenroom(Screenroom screenroom) {
        //先查询是否存在该放映厅
        Screenroom sr2 = screenRoomMapper.getScreenRoom(screenroom.getSrid());
        if (sr2 == null){
            throw new ScreenRoomServiceException("该影厅信息不存在！");
        }

        // 判断是否修改了影院，如果修改了，得判断是否存在重名
        Screenroom screenroomsBySrnameCid = screenRoomMapper.findScreenroomsBySrnameCid(screenroom.getCid(), screenroom.getSrname());
        if (!sr2.getCid().equals(screenroom.getCid()) && (screenroomsBySrnameCid != null)) {
            throw new ScreenRoomServiceException("该放映厅已存在！");
        }
        // 如果没有修改影院，则进行判断是否修改了放映厅，如果修改了，也需要判断是否存在放映厅
        if (sr2.getCid().equals(screenroom.getCid()) &&
                (!sr2.getSrname().equals(screenroom.getSrname())) &&
                (screenroomsBySrnameCid != null)){
            throw new ScreenRoomServiceException("该放映厅已存在！");
        }

        // 如果没有问题则进行修改
        if (screenRoomMapper.updateScreenRoom(screenroom) <= 0) {
            throw new ScreenRoomServiceException("修改异常！");
        }

        return RV.success("修改成功");
    }

    @Override
    public RV<PageInfo<ScreenroomDto>> getScreenrooms(ScreenroomPageQueryVO vo) {
        // 开启分页
        PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
        // 查询
        List<ScreenroomDto> screenroomDtos = screenRoomMapper.listScreenRoom(vo.getCname(),vo.getSrname());

        PageInfo<ScreenroomDto> data = new PageInfo<>(screenroomDtos);

        return RV.success("查询完毕",data);
    }

    @Override
    public RV<List<Screenroom>> getAllScreenrooms() {
        List<Screenroom> screenrooms = screenRoomMapper.getAllScreenrooms();
        return RV.success("查询完毕",screenrooms);
    }

    @Override
    public RV<List<Screenroom>> getScreenroomsByCid(String cid) {
        List<Screenroom> screenrooms = screenRoomMapper.findScreenRoomByCid(cid);
        return RV.success("查询完毕",screenrooms);
    }
}
