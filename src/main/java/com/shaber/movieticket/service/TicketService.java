package com.shaber.movieticket.service;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.TicketDto;
import com.shaber.movieticket.pojo.Ticket;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.vo.TicketAddVO;
import com.shaber.movieticket.vo.pagequery.TicketPageQueryVO;

import java.util.List;

public interface TicketService {
    // 创建订单
    RV buildTicket(String authHeader, TicketAddVO ticketAddVO);

    RV useTicket(String tid);

    RV<PageInfo<TicketDto>> listTicket(String authHeader, TicketPageQueryVO ticketPageQueryVO);

    RV<List<Ticket>> listTicketBySid(String sid);

    RV autoUse(String authHeader);
}
