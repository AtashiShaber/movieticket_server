package com.shaber.movieticket.mapper;

import com.shaber.movieticket.dto.AdminDto;
import com.shaber.movieticket.pojo.Admin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminMapper {

    @Select("select * from `admin` where phone = #{phone}")
    Admin findAdminByPhone(String phone);

    @Select("select * from `admin` where admin_name = #{adminName}")
    Admin findAdminByName(@Param("adminName") String adminName);

    @Insert("insert into `admin` values (#{admin_id},#{admin_name},#{admin_pwd},#{phone})")
    int register(@Param("admin_id") String adminId,
                 @Param("admin_name") String adminName,
                 @Param("admin_pwd") String adminPwd,
                 @Param("phone") String phone);

    List<AdminDto> listAdmin(@Param("adminName") String adminName, @Param("phone") String phone);

    @Select("select * from `admin` where admin_id = #{adminId}")
    Admin findAdmin(@Param("adminId") String adminId);

    @Update("update `admin` set admin_pwd = #{s} where admin_id = #{adminId}")
    int updatePwd(@Param("adminId") String adminId, @Param("s") String s);

    @Update("update `admin` set phone = #{newData} where admin_id = #{adminId}")
    int updatePhone(@Param("adminId") String adminId, @Param("newData") String newData);
}
