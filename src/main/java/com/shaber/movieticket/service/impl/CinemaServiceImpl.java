package com.shaber.movieticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.CinemaDto;
import com.shaber.movieticket.exception.CinemaServiceException;
import com.shaber.movieticket.mapper.*;
import com.shaber.movieticket.pojo.*;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.CinemaService;
import com.shaber.movieticket.utils.SnowflakeIdWorker;
import com.shaber.movieticket.vo.CinemaVO;
import com.shaber.movieticket.vo.pagequery.CinemaPageQueryVO;
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
public class CinemaServiceImpl implements CinemaService {

    @Autowired
    CinemaMapper cinemaMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Transactional
    @Override
    public RV addCinema(CinemaVO cinemaVO) {
        // 先查询联系方式是否重复
        if (cinemaMapper.findCinemaByCall(cinemaVO.getCcall()) != null){
            throw new CinemaServiceException("联系方式已被注册");
        }

        // 再查询地址是否被占用
        if (cinemaMapper.findCinemaByCaddress(cinemaVO.getCaddress()) != null) {
            throw new CinemaServiceException("该地址已被注册！");
        }

        //添加影院
        Cinema cinema = new Cinema();
        BeanUtils.copyProperties(cinemaVO,cinema);
        cinema.setCid(String.valueOf(snowflakeIdWorker.nextId()));
        if (cinemaMapper.addCinema(cinema) <= 0){
            throw new CinemaServiceException("添加异常!");
        }

        return RV.success("添加成功！");
    }

    @Autowired
    ScreenRoomMapper screenRoomMapper;

    @Autowired
    ScreeningMapper screeningMapper;

    @Autowired
    TicketMapper ticketMapper;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;

    @Transactional
    @Override
    public RV deleteCinema(String cid) {
        // 1. 校验影院存在性
        Cinema cinema = cinemaMapper.getCinemaByCid(cid);
        if (cinema == null) {
            throw new CinemaServiceException("影院不存在");
        }

        // 2. 获取所有放映厅
        List<Screenroom> screenrooms = screenRoomMapper.findScreenRoomByCid(cid);
        if (screenrooms.isEmpty()) {
            return RV.success("删除成功");
        }

        // 3. 批量处理放映厅数据
        processScreenRooms(screenrooms);

        return RV.success("删除成功");
    }

    private void processScreenRooms(List<Screenroom> screenrooms) {
        // 1. 收集所有放映厅ID
        List<String> srids = screenrooms.stream()
                .map(Screenroom::getSrid)
                .collect(Collectors.toList());

        // 2. 批量获取关联场次
        List<Screening> screenings = screeningMapper.selectBySrids(srids);
        if (!screenings.isEmpty()) {
            // 3. 处理未来场次退款
            processFutureScreenings(screenings);

            // 4. 批量删除历史场次数据
            cleanHistoryScreenings(screenings);
        }

        // 5. 批量删除放映厅
        screenRoomMapper.batchDelete(srids);
    }

    private void processFutureScreenings(List<Screening> screenings) {
        // 1. 过滤未来场次
        List<Screening> futureScreenings = filterFutureScreenings(screenings);
        if (futureScreenings.isEmpty()) return;

        // 2. 批量获取相关票务
        List<String> sids = futureScreenings.stream()
                .map(Screening::getSid)
                .collect(Collectors.toList());
        List<Ticket> tickets = ticketMapper.findTicketsBySids(sids);

        // 3. 处理退款逻辑
        processRefunds(tickets);

        // 4. 批量删除未来场次
        screeningMapper.batchDelete(sids);
    }

    private List<Screening> filterFutureScreenings(List<Screening> screenings) {
        final LocalDate today = LocalDate.now();
        final String currentTime = LocalTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm"));

        return screenings.stream()
                .filter(s -> isFutureScreening(s, today, currentTime))
                .collect(Collectors.toList());
    }

    private boolean isFutureScreening(Screening screening, LocalDate today, String currentTime) {
        if (screening.getSday().isAfter(today)) return true;
        if (!screening.getSday().isEqual(today)) return false;

        try {
            String startTime = screening.getStime().split("-")[0];
            return startTime.compareTo(currentTime) > 0;
        } catch (Exception e) {
            log.warn("异常时间格式 sid={} stime={}", screening.getSid(), screening.getStime());
            return false;
        }
    }

    private void processRefunds(List<Ticket> tickets) {
        // 1. 过滤未使用票
        List<String> tids = tickets.stream()
                .filter(t -> t.getTstatus() == 0)
                .map(Ticket::getTid)
                .collect(Collectors.toList());
        if (tids.isEmpty()) return;

        // 2. 批量获取订单
        List<Order> orders = orderMapper.findOrdersByTids(tids);

        // 3. 按用户聚合退款金额
        Map<String, BigDecimal> refundMap = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getUid,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Order::getOprice,
                                BigDecimal::add
                        )
                ));

        // 4. 批量退款
        refundMap.forEach((uid, amount) -> {
            if (userMapper.addBalance(uid, amount) <= 0) {
                throw new CinemaServiceException("用户退款失败: " + uid);
            }
        });

        // 5. 批量删除订单和票
        List<String> oids = orders.stream()
                .map(Order::getOid)
                .collect(Collectors.toList());
        orderMapper.batchDelete(oids);
        ticketMapper.batchDelete(tids);
    }

    private void cleanHistoryScreenings(List<Screening> screenings) {
        // 1. 收集历史场次ID
        List<String> historySids = screenings.stream()
                .map(Screening::getSid)
                .collect(Collectors.toList());

        // 2. 批量删除关联数据
        for (Ticket ticketsBySid : ticketMapper.findTicketsBySids(historySids)) {
            orderMapper.deleteOrderByTid(ticketsBySid.getTid());
        }
        ticketMapper.batchDeleteBySids(historySids);
        screeningMapper.batchDelete(historySids);
    }


    @Transactional
    @Override
    public RV updateCinema(Cinema cinema) {
        //查询是否存在该cid的cinema
        Cinema cinema1 = cinemaMapper.getCinemaByCid(cinema.getCid());
        if (cinema1 == null){
            throw new CinemaServiceException("该电影院不存在！");
        }

        // 判断电影院联系方式与地址是否改变，改变了是否地址跟联系方式被占用
        if (!cinema.getCcall().equals(cinema1.getCcall()) &&
                (cinemaMapper.findCinemaByCall(cinema.getCcall()) != null)){
            throw new CinemaServiceException("联系方式已被占用！");
        }
        if (!cinema.getCaddress().equals(cinema1.getCaddress()) &&
                (cinemaMapper.findCinemaByCaddress(cinema.getCaddress()) != null)){
            throw new CinemaServiceException("地址已被占用！");
        }

        //再进行修改
        if (cinemaMapper.updateCinema(cinema) <= 0){
            throw new CinemaServiceException("修改异常！");
        }

        return RV.success("修改成功！");
    }

    @Override
    public RV<PageInfo<CinemaDto>> listCinema(CinemaPageQueryVO cinemaPageQueryVO) {
        // 先开启分页
        PageHelper.startPage(cinemaPageQueryVO.getPageNum(),cinemaPageQueryVO.getPageSize());
        // 再进行查询
        List<Cinema> cinemas = cinemaMapper.listCinema(cinemaPageQueryVO.getCname(), cinemaPageQueryVO.getCaddress(), cinemaPageQueryVO.getCcall());
        // 转换列表元素：忽略 cid
        List<CinemaDto> dtoList = cinemas.stream()
                .map(cinema -> {
                    CinemaDto dto = new CinemaDto();
                    BeanUtil.copyProperties(cinema, dto, "cid"); // 忽略 cid
                    return dto;
                })
                .collect(Collectors.toList());

        // 构建分页结果
        PageInfo<CinemaDto> data = new PageInfo<>(dtoList);
        // 拷贝分页参数（total/pageNum/pageSize等）
        BeanUtil.copyProperties(new PageInfo<>(cinemas), data, "list");

        return RV.success("查询完毕！", data);
    }

    @Override
    public RV<PageInfo<Cinema>> listCinemaAdmin(CinemaPageQueryVO cinemaPageQueryVO) {
        // 先开启分页
        PageHelper.startPage(cinemaPageQueryVO.getPageNum(),cinemaPageQueryVO.getPageSize());
        // 再进行查询
        List<Cinema> cinemas = cinemaMapper.listCinema(cinemaPageQueryVO.getCname(), cinemaPageQueryVO.getCaddress(), cinemaPageQueryVO.getCcall());
        PageInfo<Cinema> data = new PageInfo<>(cinemas);
        return RV.success("查询完毕！",data);
    }

    @Override
    public RV<List<Cinema>> listCinemaAll() {
        List<Cinema> cinemas = cinemaMapper.listAllCinema();
        return RV.success("查询完毕",cinemas);
    }

}
