package com.shaber.movieticket.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderPayVO {
    private String oid;
    private BigDecimal oprice;
}
