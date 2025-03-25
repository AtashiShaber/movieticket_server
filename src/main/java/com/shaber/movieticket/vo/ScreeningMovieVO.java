package com.shaber.movieticket.vo;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ScreeningMovieVO {
    private String mid;
    private String cid;
    private LocalDate sday;
}
