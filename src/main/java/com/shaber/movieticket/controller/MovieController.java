package com.shaber.movieticket.controller;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.MovieDto;
import com.shaber.movieticket.dto.MoviePreviewDto;
import com.shaber.movieticket.pojo.Movie;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.MovieService;
import com.shaber.movieticket.vo.pagequery.MoviePageQueryVO;
import com.shaber.movieticket.vo.pagequery.PageQueryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController {
    @Autowired
    private final MovieService movieService;

    @PostMapping("list")
    public RV<PageInfo<MovieDto>> listMovie(@RequestBody MoviePageQueryVO moviePageQueryVO) {
        return movieService.listMovie(moviePageQueryVO);
    }

    @PostMapping("listAdmin")
    public RV<PageInfo<Movie>> listMovieAdmin(@RequestBody MoviePageQueryVO moviePageQueryVO) {
        return movieService.getMovies(moviePageQueryVO);
    }

    @PostMapping("listUpCast")
    public RV<List<Movie>> listUpCast() {
        return movieService.getMoviesByUpCast();
    }

    @PostMapping("delete")
    public RV deleteMovie(@RequestBody Map<String, String> map) {
        return movieService.deleteMovie(map.get("mid"));
    }

    @PostMapping("update")
    public RV updateMovie(@RequestBody Movie movie) {
        return movieService.updateMovie(movie.getMid(),
                movie.getMname(),
                movie.getMp(),
                movie.getMactor(),
                movie.getMstatus(),
                movie.getMstarttime(),
                movie.getMendtime());
    }

    @PostMapping("add")
    public RV<String> addMovie(@RequestBody MovieDto movieDto) {
        return movieService.addMovie(movieDto.getMname(),
                movieDto.getMp(),
                movieDto.getMactor(),
                movieDto.getMstarttime(),
                movieDto.getMendtime());
    }

    @PostMapping("downcast")
    public RV downcast(@RequestBody Map<String, String> map) {
        return movieService.downcast(map.get("mid"));
    }

    // 手动上映将开始放映的时间改为当前时间，同时如果结束放映时间比当前时间前，则改为当前时间的七天后
    @PostMapping("upcast")
    public RV upcast(@RequestBody Map<String, String> map) {
        return movieService.upcast(map.get("mid"));
    }

    @PostMapping("auto")
    public RV autoUpAndDownCast() {
        return movieService.autoUpAndDownCast();
    }

    @PostMapping("getCast")
    public RV<PageInfo<MoviePreviewDto>> getMovieCast(@RequestBody PageQueryVO pageQueryVO) {
        return movieService.getMovieCast(pageQueryVO);
    }

    @PostMapping("upComing")
    public RV<PageInfo<Movie>> getUpComing(@RequestBody PageQueryVO pageQueryVO) {
        return movieService.getUpcoming(pageQueryVO);
    }
}
