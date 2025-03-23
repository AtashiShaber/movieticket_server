package com.shaber.movieticket.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ScreeningDto {
    private String sid;
    private String mid;
    private String mname;
    private String srid;
    private String srname;
    private String cid;
    private String cname;
    private LocalDate sday;
    private String stime;
    private BigDecimal sprice;
}
