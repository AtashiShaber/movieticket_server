package com.shaber.movieticket.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketAddVO {
    private String sid;
    private String tseat;
    private int tstatus;
    private BigDecimal oprice;
}
