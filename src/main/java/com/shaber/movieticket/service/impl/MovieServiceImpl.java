package com.shaber.movieticket.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.MovieDto;
import com.shaber.movieticket.dto.MoviePreviewDto;
import com.shaber.movieticket.exception.MovieServiceException;
import com.shaber.movieticket.mapper.*;
import com.shaber.movieticket.pojo.*;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.MovieService;
import com.shaber.movieticket.utils.SnowflakeIdWorker;
import com.shaber.movieticket.vo.pagequery.MoviePageQueryVO;
import com.shaber.movieticket.vo.pagequery.PageQueryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MovieServiceImpl implements MovieService {
    @Autowired
    MovieMapper movieMapper;

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;

    @Transactional
    @Override
    public RV addMovie(String mname, String mp, String mactor, LocalDate mstarttime, LocalDate mendtime) {
        // 判断开始时间与结束时间是否正确
        if (mendtime.isBefore(mstarttime)) {
            throw new MovieServiceException("结束时间早于开始时间！");
        }

        // 添加电影
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("mname", mname);
        map.put("mp", mp);
        map.put("mactor", mactor);
        map.put("mstarttime", mstarttime);
        map.put("mendtime", mendtime);
        map.put("mid",snowflakeIdWorker.nextId());
        if (movieMapper.insertMovie(map) <= 0){
            throw new MovieServiceException("电影添加异常！");
        }

        return RV.success("添加成功！");
    }

    @Transactional
    @Override
    public RV deleteMovie(String mid) {
        //首先根据mid查询电影是否存在
        Movie movie = movieMapper.getMovieByMid(mid);
        if (movie == null){
            throw new MovieServiceException("该电影不存在！");
        }

        //在查询电影是否正在上映，如果在上映则无法删除，需要先进行下映操作
        if (movie.getMstatus() == 1){
            throw new MovieServiceException("电影正在上映，如果要进行删除操作，需要对电影先进行下映");
        }

        // 进行删除操作
        if (movieMapper.deleteMovie(mid) <= 0) {
            throw new MovieServiceException("删除异常！");
        }

        return RV.success("删除成功！");
    }

    @Transactional
    @Override
    public RV updateMovie(String mid, String mname, String mp, String mactor, int mstatus, LocalDate mstarttime, LocalDate mendtime) {
        //首先根据mid查询电影是否存在
        Movie movie = movieMapper.getMovieByMid(mid);
        if (movie == null){
            throw new MovieServiceException("无法查询到该电影！");
        }

        // 判断开始时间与结束时间是否正确
        if (mendtime.isBefore(mstarttime)) {
            throw new MovieServiceException("结束时间早于开始时间！");
        }

        //传入修改后的电影信息
        if (movieMapper.updateMovie(mid,mname,mp,mactor,mstatus,mstarttime,mendtime) <= 0){
            throw new MovieServiceException("修改异常！");
        }

        return RV.success("修改成功！");
    }

    @Override
    public RV<Movie> getMovie(String mid) {
        Movie movie = movieMapper.getMovieByMid(mid);
        return RV.success("查询完毕",movie);
    }

    @Override
    public RV<PageInfo<Movie>> getMovies(MoviePageQueryVO vo) {
        // 开启分页
        PageHelper.startPage(vo.getPageNum(),vo.getPageSize());

        //进行查询
        Map<String,Object> map = new HashMap<>();
        map.put("mname",vo.getMname());
        map.put("mactor",vo.getMactor());
        map.put("mstarttime",vo.getMstarttime());
        map.put("mendtime",vo.getMendtime());
        map.put("mstatus",vo.getMstatus());
        List<Movie> movies = movieMapper.listMovie(map);
        PageInfo<Movie> data = new PageInfo<>(movies);
        return RV.success("查询完毕！",data);
    }

    @Autowired
    ScreeningMapper screeningMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    TicketMapper ticketMapper;

    @Autowired
    OrderMapper orderMapper;

    @Transactional
    @Override
    public RV downcast(String mid) {
        // 1. 校验电影存在性
        Movie movie = movieMapper.getMovieByMid(mid);
        if (movie == null) {
            throw new MovieServiceException("电影不存在");
        }

        // 2. 获取当前时间基准（保证事务内时间一致）
        final LocalDate today = LocalDate.now();
        final LocalTime now = LocalTime.now();
        final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        // 3. 查询所有关联场次（保持原Mapper方法）
        List<Screening> screenings = screeningMapper.findScreeningsByMid(mid);

        // 4. 过滤未来场次（内存处理）
        List<Screening> futureScreenings = screenings.stream()
                .filter(screening -> isFutureScreening(screening, today, now, timeFormat))
                .collect(Collectors.toList());

        // 5. 处理退款逻辑
        processRefunds(futureScreenings);

        // 6. 更新电影状态（保持原Mapper方法）
        if (movieMapper.downCast(mid, 2, today) <= 0) {
            throw new MovieServiceException("电影状态更新失败");
        }

        return RV.success("下映成功");
    }

    // 在Service层获取当前时间字符串
    final String currentTimeStr = LocalTime.now()
            .format(DateTimeFormatter.ofPattern("HH:mm"));

    private boolean isFutureScreening(Screening screening, LocalDate today,
                                      LocalTime now, DateTimeFormatter formatter) {
        // 日期判断优先
        if (screening.getSday().isAfter(today)) return true;
        if (!screening.getSday().isEqual(today)) return false;

        // 解析时间字符串
        try {
            String[] timeParts = screening.getStime().split("-");
            if (timeParts.length != 2) return false;

            LocalTime startTime = LocalTime.parse(timeParts[0], formatter);
            return startTime.isAfter(now);
        } catch (Exception e) {
            log.warn("场次时间格式异常 sid={} stime={}", screening.getSid(), screening.getStime());
            return false;
        }
    }

    private void processRefunds(List<Screening> futureScreenings) {
        if (futureScreenings.isEmpty()) return;

        // 1. 批量获取票务数据
        List<String> sids = futureScreenings.stream()
                .map(Screening::getSid)
                .collect(Collectors.toList());
        List<Ticket> tickets = ticketMapper.findTicketsBySids(sids);

        // 2. 过滤未使用票并收集ID
        List<String> refundTicketIds = tickets.stream()
                .filter(t -> t.getTstatus() == 0)
                .map(Ticket::getTid)
                .collect(Collectors.toList());

        if (refundTicketIds.isEmpty()) return;

        // 3. 批量获取订单信息
        List<Order> orders = orderMapper.findOrdersByTids(refundTicketIds);

        // 4. 按用户聚合退款金额（保证精度）
        Map<String, BigDecimal> userRefunds = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getUid,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Order::getOprice,
                                BigDecimal::add
                        )
                ));

        // 5. 批量处理用户退款
        // 使用setScale保证精度
        userRefunds.forEach((uid, amount) -> {
            BigDecimal refundAmount = amount.setScale(2, RoundingMode.HALF_UP);
            if (userMapper.addBalance(uid, refundAmount) <= 0) {
                throw new MovieServiceException("用户退款失败 uid: " + uid);
            }
        });

        // 6. 批量更新订单状态
        List<String> orderIds = orders.stream()
                .map(Order::getOid)
                .collect(Collectors.toList());
        if (!orderIds.isEmpty() && orderMapper.batchUpdateStatus(orderIds, 2) != orderIds.size()) {
            throw new MovieServiceException("部分订单状态更新失败");
        }

        // 7. 批量更新票状态
        if (ticketMapper.batchUpdateStatus(refundTicketIds, 2) != refundTicketIds.size()) {
            throw new MovieServiceException("部分票状态更新失败");
        }

        // 8. 删除场次
        if (screeningMapper.batchDelete(sids) != sids.size()) {
            throw new MovieServiceException("部分场次删除失败");
        }
    }


    @Transactional
    @Override
    public RV upcast(String mid) {
        //检查该电影是否存在
        Movie movie = movieMapper.getMovieByMid(mid);
        if (movie == null){
            throw new MovieServiceException("电影不存在！");
        }

        if (movieMapper.upCast(mid,1) <= 0){
            throw new MovieServiceException("上映异常！");
        }

        return RV.success("上映成功！");
    }

    @Transactional
    @Override
    public RV autoUpAndDownCast() {
        if ((movieMapper.autoUpCast(LocalDate.now()) + movieMapper.autoDownCast(LocalDate.now())) <= 0){
            return RV.success("当前不存在需要操作的电影");
        }
        return RV.success("操作成功");
    }

    @Override
    public RV<PageInfo<Movie>> getUpcoming(PageQueryVO pageQueryVO) {
        //获取即将上映的电影（距离上映还剩7天及以内时间的电影）
        PageHelper.startPage(pageQueryVO.getPageNum(),pageQueryVO.getPageSize());
        List<Movie> movies = movieMapper.findUpcoming(LocalDate.now());
        PageInfo<Movie> data = new PageInfo<>(movies);
        return RV.success("查询完毕！",data);
    }

    @Override
    public RV<PageInfo<MovieDto>> listMovie(MoviePageQueryVO vo) {
        // 开启分页
        PageHelper.startPage(vo.getPageNum(),vo.getPageSize());

        //进行查询
        Map<String,Object> map = new HashMap<>();
        map.put("mname",vo.getMname());
        map.put("mactor",vo.getMactor());
        map.put("mstarttime",vo.getMstarttime());
        map.put("mendtime",vo.getMendtime());
        map.put("mstatus",vo.getMstatus());
        List<Movie> movies = movieMapper.listMovie(map);
        // 转换列表元素
        List<MovieDto> dtoList = movies.stream()
                .map(movie -> {
                    MovieDto dto = new MovieDto();
                    BeanUtil.copyProperties(movie, dto, "mid"); // 忽略 mid
                    return dto;
                })
                .collect(Collectors.toList());

        // 构建分页结果
        PageInfo<MovieDto> data = new PageInfo<>(dtoList);
        // 拷贝分页参数（total/pageNum/pageSize等）
        BeanUtil.copyProperties(new PageInfo<>(movies), data, "list");

        return RV.success("查询完毕！", data);
    }

    @Override
    public RV<List<Movie>> getMoviesByUpCast() {
        List<Movie> movies = movieMapper.listMoviesUpCase();
        return RV.success("查询完毕",movies);
    }

    @Override
    public RV<PageInfo<MoviePreviewDto>> getMovieCast(PageQueryVO pageQueryVO) {
        PageHelper.startPage(pageQueryVO.getPageNum(),pageQueryVO.getPageSize());
        List<MoviePreviewDto> movies = movieMapper.findCast(LocalDate.now());
        PageInfo<MoviePreviewDto> data = new PageInfo<>(movies);
        return RV.success("查询完毕！",data);
    }

}
