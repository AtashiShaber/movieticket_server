package com.shaber.movieticket.controller;

import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.OrderService;
import com.shaber.movieticket.vo.OrderAddVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("countToday")
    public RV<Integer> countToday() {
        return orderService.countOrdersToday();
    }

    @PostMapping("countTodaySales")
    public RV<BigDecimal> countTodaySales() {
        return orderService.countOrdersTodaySales();
    }

    @PostMapping("refundRate")
    public RV<Float> refundRate() {
        return orderService.refundRate();
    }

    @PostMapping("refund")
    public RV refund(@RequestHeader("Authorization") String authHeader,
                     @RequestBody Map<String, String> orderMap) {
        return orderService.refund(authHeader, orderMap.get("oid"));
    }

    @PostMapping("build")
    public RV buildOrder(@RequestHeader("Authorization") String authHeader, @RequestBody OrderAddVO orderAddVO) {
        return orderService.buildOrder(authHeader, orderAddVO);
    }
}
