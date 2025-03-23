package com.shaber.movieticket.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TicketDto {
    private String tid;
    private String mname;
    private String cname;
    private String srname;
    private LocalDate sday;
    private String tseat;
    private int tstatus;
}
