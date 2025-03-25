package com.shaber.movieticket.service;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.OrderDto;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.vo.OrderAddVO;
import com.shaber.movieticket.vo.OrderPayVO;
import com.shaber.movieticket.vo.pagequery.PageQueryVO;

import java.math.BigDecimal;

public interface OrderService {

    RV<Integer> countOrdersToday();

    RV<BigDecimal> countOrdersTodaySales();

    RV<Float> refundRate();

    RV refund(String authHeader, String oid);

    RV buildOrder(String authHeader, OrderAddVO orderAddVO);

    RV pay(String authHeader, OrderPayVO orderAddVO);

    RV<PageInfo<OrderDto>> listOrder(String authHeader, PageQueryVO pageQueryVO);
}
