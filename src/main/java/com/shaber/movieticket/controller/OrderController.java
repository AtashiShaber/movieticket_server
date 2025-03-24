package com.shaber.movieticket.controller;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.OrderDto;
import com.shaber.movieticket.resp.RV;
import com.shaber.movieticket.service.OrderService;
import com.shaber.movieticket.vo.OrderAddVO;
import com.shaber.movieticket.vo.pagequery.PageQueryVO;
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
    public RV buildOrder(@RequestHeader("Authorization") String authHeader,
                         @RequestBody OrderAddVO orderAddVO) {
        return orderService.buildOrder(authHeader, orderAddVO);
    }

    // 若是成功生成票之后还需要确认付款
    @PostMapping("pay")
    public RV pay(@RequestHeader("Authorization") String authHeader,
                  @RequestBody OrderAddVO orderAddVO) {
        return orderService.pay(authHeader, orderAddVO);
    }

    // 查询用户订单信息，包含分页查询
    @PostMapping("list")
    public RV<PageInfo<OrderDto>> list(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody PageQueryVO pageQueryVO) {
        return orderService.listOrder(authHeader, pageQueryVO);
    }
}
