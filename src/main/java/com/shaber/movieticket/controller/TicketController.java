package com.shaber.movieticket.controller;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.TicketDto;
import com.shaber.movieticket.pojo.Ticket;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.TicketService;
import com.shaber.movieticket.vo.TicketAddVO;
import com.shaber.movieticket.vo.pagequery.TicketPageQueryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ticket")
public class TicketController {
    @Autowired
    private final TicketService ticketService;

    // 使用票
    @PostMapping("/useTicket")
    public RV useTicket(@RequestBody Map<String, String> ticketMap) {
        return ticketService.useTicket(ticketMap.get("tid"));
    }

    // 查询票务信息
    @PostMapping("/list")
    public RV<PageInfo<TicketDto>> listTicket(@RequestHeader("Authorization") String authHeader,
                                              @RequestBody TicketPageQueryVO ticketPageQueryVO) {
        return ticketService.listTicket(authHeader, ticketPageQueryVO);
    }

    // 创建票
    @PostMapping("/build")
    public RV buildTicket(@RequestHeader("Authorization") String authHeader,
                          @RequestBody TicketAddVO ticketAddVO){
        return ticketService.buildTicket(authHeader, ticketAddVO);
    }

    // 搜索指定放映场次的票的信息
    @PostMapping("/listBySid")
    public RV<List<Ticket>> listTicketsBySid(@RequestBody Map<String, String> map) {
        return ticketService.listTicketBySid(map.get("sid"));
    }

    // 检测时间自动使用票
    @PostMapping("autoUse")
    public RV autoUse(@RequestHeader("Authorization") String authHeader) {
        return ticketService.autoUse(authHeader);
    }
}
