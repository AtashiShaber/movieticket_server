package com.shaber.movieticket.service;

import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.vo.OrderAddVO;

import java.math.BigDecimal;

public interface OrderService {

    RV<Integer> countOrdersToday();

    RV<BigDecimal> countOrdersTodaySales();

    RV<Float> refundRate();

    RV refund(String authHeader, String oid);

    RV buildOrder(String authHeader, OrderAddVO orderAddVO);

    RV pay(String authHeader, OrderAddVO orderAddVO);
}
