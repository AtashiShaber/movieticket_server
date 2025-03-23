package com.shaber.movieticket.service;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.CinemaDto;
import com.shaber.movieticket.pojo.Cinema;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.vo.CinemaVO;
import com.shaber.movieticket.vo.pagequery.CinemaPageQueryVO;

import java.util.List;

public interface CinemaService {
    //增
    RV addCinema(CinemaVO cinemaVO);
    //删
    RV deleteCinema(String cid);
    //改
    RV updateCinema(Cinema cinema);
    //查
    RV<PageInfo<CinemaDto>> listCinema(CinemaPageQueryVO cinemaPageQueryVO);
    // 查 管理员
    RV<PageInfo<Cinema>> listCinemaAdmin(CinemaPageQueryVO cinemaPageQueryVO);

    RV<List<Cinema>> listCinemaAll();
}
