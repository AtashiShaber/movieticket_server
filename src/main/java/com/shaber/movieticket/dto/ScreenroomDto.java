package com.shaber.movieticket.dto;

import com.shaber.movieticket.pojo.Screenroom;
import lombok.Data;

@Data
public class ScreenroomDto extends Screenroom {
    private String cname;
}
