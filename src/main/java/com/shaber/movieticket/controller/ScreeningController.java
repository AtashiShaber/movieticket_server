package com.shaber.movieticket.controller;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.ScreeningDto;
import com.shaber.movieticket.pojo.Screening;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.ScreeningService;
import com.shaber.movieticket.vo.ScreeningMovieVO;
import com.shaber.movieticket.vo.ScreeningVO;
import com.shaber.movieticket.vo.pagequery.ScreeningPageQueryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/screening")
@RequiredArgsConstructor
public class ScreeningController {

    @Autowired
    private final ScreeningService screeningService;

    @PostMapping("list")
    public RV<PageInfo<ScreeningDto>> listScreenings(@RequestBody ScreeningPageQueryVO vo) {
        return screeningService.getScreenings(vo);
    }

    @PostMapping("listByCid")
    public RV<List<Screening>> listScreeningsByCid(@RequestBody ScreeningVO screeningVO) {
        return screeningService.getScreeningsByCid(screeningVO.getSrid(),screeningVO.getSday());
    }

    @PostMapping("listByMid")
    public RV<List<ScreeningDto>> listScreeningsByMid(@RequestBody ScreeningMovieVO screeningMovieVO) {
        return screeningService.getScreeningsByMid(screeningMovieVO);
    }

    @PostMapping("add")
    public RV addScreening(@RequestBody ScreeningVO vo) {
        return screeningService.addScreening(vo);
    }

    @PostMapping("delete")
    public RV deleteScreening(@RequestBody Map<String, String> map) {
        return screeningService.deleteScreening(map.get("sid"));
    }

    @PostMapping("update")
    public RV updateScreening(@RequestBody Screening screening) {
        return screeningService.updateScreening(screening);
    }

    @PostMapping("countToday")
    public RV<Integer> countToday() {
        return screeningService.countToday();
    }

}
