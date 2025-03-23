package com.shaber.movieticket.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.ScreeningDto;
import com.shaber.movieticket.exception.CinemaServiceException;
import com.shaber.movieticket.exception.ScreeningServiceException;
import com.shaber.movieticket.mapper.OrderMapper;
import com.shaber.movieticket.mapper.ScreeningMapper;
import com.shaber.movieticket.mapper.TicketMapper;
import com.shaber.movieticket.mapper.UserMapper;
import com.shaber.movieticket.pojo.*;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.ScreeningService;
import com.shaber.movieticket.utils.SnowflakeIdWorker;
import com.shaber.movieticket.vo.ScreeningVO;
import com.shaber.movieticket.vo.pagequery.ScreeningPageQueryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class ScreeningServiceImpl implements ScreeningService {
    @Autowired
    ScreeningMapper screeningMapper;

    @Autowired
    TicketMapper ticketMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Override
    public RV addScreening(ScreeningVO screeningVO) {
        //首先查询放映是否出现冲突
        if (screeningMapper.findScreeningBySridDate(screeningVO.getSrid(),screeningVO.getSday(),screeningVO.getStime()) != null) {
            throw new ScreeningServiceException("放映冲突！");
        }

        //如果未发生冲突则添加放映信息
        Screening screening = new Screening();
        BeanUtils.copyProperties(screeningVO,screening);
        screening.setSid(String.valueOf(snowflakeIdWorker.nextId()));
        if (screeningMapper.insertScreening(screening) <= 0){
            throw new ScreeningServiceException("放映添加异常！");
        }

        return RV.success("添加成功！");
    }

    @Transactional
    @Override
    public RV deleteScreening(String sid) {
        // 查询放映场次是否存在
        Screening screening = screeningMapper.getScreening(sid);
        if (screening == null) {
            throw new ScreeningServiceException("放映信息不存在！");
        }

        // 批量获取该场次所有票务信息
        List<Ticket> tickets = ticketMapper.findTicketsBySid(sid);
        if (!tickets.isEmpty()) {
            // 分割需要退款的票务（状态为未使用）
            List<String> refundTids = tickets.stream()
                    .filter(t -> t.getTstatus() == 0)
                    .map(Ticket::getTid)
                    .collect(Collectors.toList());

            // 批量处理退款逻辑
            if (!refundTids.isEmpty()) {
                // 批量获取关联订单
                List<Order> refundOrders = orderMapper.findOrdersByTids(refundTids);

                // 按用户聚合退款金额（使用并行流提升大数据集处理效率）
                ConcurrentMap<String, BigDecimal> userRefundMap = refundOrders.parallelStream()
                        .collect(Collectors.groupingByConcurrent(
                                Order::getUid,
                                Collectors.mapping(
                                        Order::getOprice,
                                        Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                                )
                        ));

                // 批量更新用户余额（使用并行处理提升性能）
                userRefundMap.forEach((uid, amount) -> {
                    if (userMapper.addBalance(uid, amount) <= 0) {
                        throw new ScreeningServiceException("用户退款失败: " + uid);
                    }
                });

                // 批量更新订单状态（使用批量操作）
                List<String> oids = refundOrders.stream()
                        .map(Order::getOid)
                        .collect(Collectors.toList());
                if (orderMapper.batchUpdateStatus(oids, 2) != oids.size()) {
                    throw new ScreeningServiceException("订单状态更新不完整");
                }
            }

            // 批量更新所有票务状态（包含已使用票务）
            List<String> allTids = tickets.stream()
                    .map(Ticket::getTid)
                    .collect(Collectors.toList());
            int updateCount = ticketMapper.batchUpdateStatus(allTids, 2);
            if (updateCount != allTids.size()) {
                throw new ScreeningServiceException("票务状态更新异常，预期更新:" + allTids.size() + " 实际更新:" + updateCount);
            }
        }

        // 删除放映记录（使用主键直接删除）
        if (screeningMapper.deleteScreening(sid) <= 0) {
            throw new ScreeningServiceException("放映场次删除失败");
        }

        return RV.success("删除操作已完成");
    }

    @Override
    public RV updateScreening(Screening screening) {
//        首先查询是否存在该放映信息
        Screening testScreening = screeningMapper.getScreening(screening.getSid());
        if (testScreening == null){
            throw new ScreeningServiceException("放映信息不存在!");
        }

        // 再判断是否改变了部分放映信息，由此判断是否产生了冲突
        Screening screeningBySridDate = screeningMapper.findScreeningBySridDate(screening.getSrid(), screening.getSday(), screening.getStime());
        if ((screeningBySridDate != null) &&
                !testScreening.getSrid().equals(screening.getSrid())) {
            throw new CinemaServiceException("该时间段的放映厅已被占用！");
        }
        if ((screeningBySridDate != null) &&
                testScreening.getSrid().equals(screening.getSrid()) &&
                (!screening.getSid().equals(screeningBySridDate.getSid()))) {
            throw new CinemaServiceException("该时间段的放映厅已被占用！");
        }

        //如果存在则对放映信息进行修改
        if (screeningMapper.updateScreening(screening) <= 0){
            throw new ScreeningServiceException("放映记录修改失败!");
        }

        return RV.success("修改成功！");
    }

    @Override
    public RV<PageInfo<ScreeningDto>> getScreenings(ScreeningPageQueryVO vo) {
        // 开启分页
        PageHelper.startPage(vo.getPageNum(),vo.getPageSize());

        // 进行查询
        List<ScreeningDto> screeningDtos = screeningMapper.listSreening(vo.getMnmae(), vo.getSrname(), vo.getCname(), vo.getSday(), vo.getStime());
        PageInfo<ScreeningDto> data = new PageInfo<>(screeningDtos);

        return RV.success("查询完毕",data);
    }

    @Override
    public RV<List<Screening>> getScreeningsByCid(String srid, LocalDate sday) {
        List<Screening> screenings = screeningMapper.findScreeningByCid(srid,sday);
        return RV.success("查询完成！",screenings);
    }

    @Override
    public RV<Integer> countToday() {
        return RV.success("统计完成！",screeningMapper.countToday());
    }
}
