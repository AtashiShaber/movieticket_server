package com.shaber.movieticket.controller;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.ScreenroomDto;
import com.shaber.movieticket.pojo.Screenroom;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.ScreenroomService;
import com.shaber.movieticket.vo.ScreenroomVO;
import com.shaber.movieticket.vo.pagequery.ScreenroomPageQueryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/screenroom")
@RequiredArgsConstructor
public class ScreenroomController {
    @Autowired
    private ScreenroomService screenroomService;

    @PostMapping("list")
    public RV<PageInfo<ScreenroomDto>> listScreenroom(@RequestBody ScreenroomPageQueryVO vo) {
        return screenroomService.getScreenrooms(vo);
    }

    @PostMapping("listAll")
    public RV<List<Screenroom>> listAllScreenroom() {
        return screenroomService.getAllScreenrooms();
    }

    @PostMapping("listCid")
    public RV<List<Screenroom>> listCidScreenroom(@RequestBody Map<String ,String> map) {
        return screenroomService.getScreenroomsByCid(map.get("cid"));
    }

    @PostMapping("add")
    public RV addScreenroom(@RequestBody ScreenroomVO screenroomVO) {
        return screenroomService.addScreenroom(screenroomVO);
    }

    @PostMapping("update")
    public RV updateScreenroom(@RequestBody Screenroom screenroom) {
        return screenroomService.updateScreenroom(screenroom);
    }
}
