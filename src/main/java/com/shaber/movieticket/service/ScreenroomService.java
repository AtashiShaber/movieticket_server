package com.shaber.movieticket.service;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.ScreenroomDto;
import com.shaber.movieticket.pojo.ResultValue;
import com.shaber.movieticket.pojo.Screenroom;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.vo.ScreenroomVO;
import com.shaber.movieticket.vo.pagequery.ScreenroomPageQueryVO;

import java.util.List;

public interface ScreenroomService {
    //增
    RV addScreenroom(ScreenroomVO screenroomVO);
    //删
    RV deleteScreenroom(String srid);
    //改 修改该电影院的放映厅名字
    RV updateScreenroom(Screenroom screenroom);
    //查 模糊查询
    RV<PageInfo<ScreenroomDto>> getScreenrooms(ScreenroomPageQueryVO vo);

    RV<List<Screenroom>> getAllScreenrooms();

    RV<List<Screenroom>> getScreenroomsByCid(String cid);
}
