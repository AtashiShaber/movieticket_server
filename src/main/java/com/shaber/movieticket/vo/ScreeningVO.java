package com.shaber.movieticket.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScreeningVO {
    private String mid;
    private String srid;
    private LocalDate sday;
    private String stime;
    private BigDecimal sprice;
}
