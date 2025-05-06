package com.shaber.movieticket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String uid;
    private String uname;
    private String upwd;
    private BigDecimal umoney;
    private String uphone;
}
