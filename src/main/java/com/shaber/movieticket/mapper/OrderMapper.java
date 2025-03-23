package com.shaber.movieticket.mapper;

import com.shaber.movieticket.pojo.Order;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    //查询该用户的所有订单（支持模糊查询）
    List<Order> listOrder(Map<String, Object> map);

    //获取所有订单
    @Select("select * from `order`")
    List<Order> listOrderAll();

    //通过oid查询指定的订单
    @Select("select * from `order` where oid = #{oid}")
    Order getOrderByOid(@Param("oid") String oid);

    //增加订单
    @Insert("insert into `order` (uid,tid,otime,oprice,ostatus) " +
            "values (#{uid},#{tid},#{otime},#{oprice},#{ostatus})")
    int insertOrder(Order order);

    //删除订单
    @Delete("delete from `order` where oid = #{oid}")
    int deleteOrder(@Param("oid") String oid);

    @Select("select * from `order` where tid = #{tid}")
    Order findOrderByTid(@Param("tid") String tid);

    @Update("update `order` set ostatus = #{i} where oid = #{oid}")
    int updateOrder(@Param("oid") String oid, @Param("i") int i);

    @Update("<script>" +
            "UPDATE `order` SET ostatus = #{status} WHERE oid IN " +
            "<foreach item='id' collection='oids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("oids") List<String> oids,
                          @Param("status") int status);

    // 批量查询订单信息
    List<Order> findOrdersByTids(@Param("tids") List<String> tids);

    @Delete("delete from `order` where tid = #{tid}")
    int deleteOrderByTid(@Param("tid") String tid);

    int batchDelete(List<String> oids);

    @Select("select count(1) from `order` where DATE(otime) = CURDATE() and ostatus = 1")
    int countOrderToday();

    @Select("select * from `order` where DATE(otime) = CURDATE() and ostatus = 1")
    List<Order> countOrdersToday();

    @Select("select count(1) from `order` where DATE(otime) = CURDATE() and ostatus > 0")
    int countOrderTodayAllPaid();

    @Insert("insert into `order` values (#{oid},#{uid},#{now},#{tid},#{oprice},#{ostatus})")
    int buildOrder(@Param("oid") String oid,
                   @Param("uid") String uid,
                   @Param("now") LocalDateTime now,
                   @Param("tid") String tid,
                   @Param("oprice") BigDecimal oprice,
                   @Param("ostatus") int ostatus);
}
