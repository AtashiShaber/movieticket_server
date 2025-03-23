package com.shaber.movieticket.service;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.MovieDto;
import com.shaber.movieticket.dto.MoviePreviewDto;
import com.shaber.movieticket.pojo.Movie;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.vo.pagequery.MoviePageQueryVO;
import com.shaber.movieticket.vo.pagequery.PageQueryVO;

import java.time.LocalDate;
import java.util.List;

public interface MovieService {
    //增
    RV addMovie(String mname, String mp, String mactor, LocalDate mstarttime, LocalDate mendtime);
    //删 根据mid删除
    RV deleteMovie(String mid);
    //改
    RV updateMovie(String mid, String mname, String mp, String mactor, int mstatus, LocalDate mstarttime, LocalDate mendtime);
    //查 根据mid进行精准查询
    RV<Movie> getMovie(String mid);
    //查 进行模糊查询
    RV<PageInfo<Movie>> getMovies(MoviePageQueryVO moviePageQueryVO);

    //Other
    //对电影进行下映操作
    RV downcast(String mid);
    //对电影进行上映操作
    RV upcast(String mid);
    //对在日期内的电影执行上映操作，日期外的电影执行下映操作
    RV autoUpAndDownCast();

    //获取即将上映的电影
    RV<PageInfo<Movie>> getUpcoming(PageQueryVO pageQueryVO);

    RV<PageInfo<MovieDto>> listMovie(MoviePageQueryVO moviePageQueryVO);

    RV<List<Movie>> getMoviesByUpCast();

    // 获取当天正在上映的电影
    RV<PageInfo<MoviePreviewDto>> getMovieCast(PageQueryVO pageQueryVO);
}
