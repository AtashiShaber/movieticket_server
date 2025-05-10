package com.shaber.movieticket.mapper;

import com.shaber.movieticket.dto.UserDto;
import com.shaber.movieticket.pojo.User;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface UserMapper {
    //检查该用户名的用户是否存在
    User getUserByUname(String uname);

    // 通过用户名跟手机号模糊查询用户
    List<UserDto> listUser(@Param("uname") String uname, @Param("uphone") String uphone);

    // 通过用户手机号查询用户
    @Select("select * from `user` where uphone = #{uphone}")
    User findUserByPhone(@Param("uphone") String uphone);

    @Insert("insert into `user` (uid,uname,upwd,uphone) values (#{uid},#{uname},#{upwd},#{uphone})")
    int register(@Param("uid") String uid, @Param("uname") String uname, @Param("upwd") String upwd, @Param("uphone") String uphone);

    @Update("update `user` set upwd = #{upwd} where uid = #{uid}")
    int updatePassword(@Param("uid") String uid, @Param("upwd") String upwd);

    @Update("update `user` set uphone = #{newPhone} where uid = #{uid}")
    int updatePhone(@Param("uid") String uid, @Param("newPhone") String newPhone);

    @Select("select * from `user` where uid = #{uid}")
    User findUserByUid(@Param("uid") String uid);

    @Update("update `user` set uname = #{uname}," +
            "uphone = #{uphone}," +
            "umoney = #{umoney} " +
            "where uid = #{uid}")
    int updateUser(UserDto userDto);

    @Delete("delete from `user` where uid = #{uid}")
    int deleteUser(@Param("uid") String uid);

    @Update("update `user` set umoney = umoney + #{oprice} where uid = #{uid}")
    int backMoney(@Param("uid") String uid, @Param("oprice") BigDecimal oprice);

    @Update("UPDATE user SET umoney = umoney + #{amount} " +
            "WHERE uid = #{uid}")
    int addBalance(@Param("uid") String uid,
                   @Param("amount") BigDecimal amount);

    @Update("UPDATE user SET umoney = umoney - #{oprice} " +
            "WHERE uid = #{uid}")
    int pay(@Param("uid") String uid, @Param("oprice") BigDecimal oprice);

    @Select("SELECT * from `user` where uname = #{uname}")
    User findUserByUname(@Param("uname") String username);
}
