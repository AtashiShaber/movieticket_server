package com.shaber.movieticket.vo.pagequery;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TicketPageQueryVO extends PageQueryVO{
    private String mname;
    private String cname;
    private String srname;
    private LocalDate sday;
}
