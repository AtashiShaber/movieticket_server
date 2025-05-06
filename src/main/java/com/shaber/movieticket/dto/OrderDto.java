package com.shaber.movieticket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDto {
    private String oid;
    private String tid;
    private String mname;
    private LocalDateTime otime;
    private BigDecimal oprice;
    private int ostatus;
    private int tstatus;
}
