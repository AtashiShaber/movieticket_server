package com.shaber.movieticket.service;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.ScreeningDto;
import com.shaber.movieticket.pojo.Screening;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.vo.ScreeningMovieVO;
import com.shaber.movieticket.vo.ScreeningVO;
import com.shaber.movieticket.vo.pagequery.ScreeningPageQueryVO;

import java.time.LocalDate;
import java.util.List;

public interface ScreeningService {
    //增
    RV addScreening(ScreeningVO screeningVO);
    //删
    RV deleteScreening(String sid);
    //改
    RV updateScreening(Screening screening);

    //查 联合查询（模糊查询）
    RV<PageInfo<ScreeningDto>> getScreenings(ScreeningPageQueryVO vo);

    RV<List<Screening>> getScreeningsByCid(String srid, LocalDate sday);

    RV<Integer> countToday();

    RV<List<ScreeningDto>> getScreeningsByMid(ScreeningMovieVO map);
}
