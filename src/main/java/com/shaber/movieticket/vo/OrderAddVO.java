package com.shaber.movieticket.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderAddVO {
    private String tid;
    private BigDecimal oprice;
    private int ostatus;
}
