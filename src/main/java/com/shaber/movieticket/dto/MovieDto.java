package com.shaber.movieticket.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieDto {
    private String mname;
    private String mp;
    private String mactor;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate mstarttime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate mendtime;

}
