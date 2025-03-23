package com.shaber.movieticket.controller;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.CinemaDto;
import com.shaber.movieticket.pojo.Cinema;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.CinemaService;
import com.shaber.movieticket.vo.CinemaVO;
import com.shaber.movieticket.vo.pagequery.CinemaPageQueryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cinema")
public class CinemaController {

    @Autowired
    private final CinemaService cinemaService;

    @PostMapping("list")
    public RV<PageInfo<CinemaDto>> listCinema(@RequestBody CinemaPageQueryVO cinemaPageQueryVO) {
        return cinemaService.listCinema(cinemaPageQueryVO);
    }

    @PostMapping("listAdmin")
    public RV<PageInfo<Cinema>> listCinemaAdmin(@RequestBody CinemaPageQueryVO cinemaPageQueryVO) {
        return cinemaService.listCinemaAdmin(cinemaPageQueryVO);
    }

    @PostMapping("listAll")
    public RV<List<Cinema>> listAllCinema() {
        return cinemaService.listCinemaAll();
    }

    @PostMapping("add")
    public RV addCinema(@RequestBody CinemaVO cinemaVO) {
        return cinemaService.addCinema(cinemaVO);
    }

    @PostMapping("update")
    public RV updateCinema(@RequestBody Cinema cinema) {
        return cinemaService.updateCinema(cinema);
    }

    @PostMapping("delete")
    public RV deleteCinema(@RequestBody Map<String, String> map) {
        return cinemaService.deleteCinema(map.get("cid"));
    }
}
