package com.shaber.movieticket.mapper;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.pojo.Cinema;
import com.shaber.movieticket.vo.CinemaVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CinemaMapper {
    //查询电影院（通过名字与电话）
    Cinema getCinema(@Param("cname") String cname, @Param("ccall") String ccall);
    //查询电影院（通过电影院ID）
    Cinema getCinemaByCid(@Param("cid") String cid);
    //查询所有电影院
    List<Cinema> listCinema(@Param("cname") String cname,
                                @Param("caddress") String caddress,
                                @Param("ccall") String ccall);

    //增
    int addCinema(Cinema cinema);

    //删
    int deleteCinema(@Param("cid") String cid);

    //改
    int updateCinema(Cinema cinema);

    @Select("select * from `cinema` where ccall = #{ccall}")
    Cinema findCinemaByCall(@Param("ccall") String ccall);

    @Select("select * from `cinema` where caddress = #{caddress}")
    Cinema findCinemaByCaddress(@Param("caddress") String caddress);

    @Select("select * from `cinema`")
    List<Cinema> listAllCinema();
}
