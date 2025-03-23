package com.shaber.movieticket.mapper;

import com.shaber.movieticket.dto.ScreenroomDto;
import com.shaber.movieticket.pojo.Screenroom;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScreenRoomMapper {
    // 模糊查询
    List<ScreenroomDto> listScreenRoom(@Param("cname") String cname, @Param("srname") String srname);

    // 通过srid制定查询screenroom
    @Select("select * from screenroom where srid = #{srid}")
    Screenroom getScreenRoom(String srid);

    // 增加
    @Insert("insert into screenroom values (#{srid},#{cid},#{srname})")
    int insertScreenRoom(Screenroom screenroom);

    @Select("select * from `screenroom` where cid = #{cid}")
    List<Screenroom> findScreenRoomByCid(@Param("cid") String cid);

    @Delete("delete * from `screenroom` where srid = #{srid}")
    int deleteScreenroom(@Param("srid") String srid);

    int batchDelete(@Param("srids") List<String> srids);

    @Select("select * from `screenroom`")
    List<Screenroom> getAllScreenrooms();

    @Select("select * from `screenroom` where cid = #{cid} and srname = #{srname}")
    Screenroom findScreenroomsBySrnameCid(@Param("cid") String cid, @Param("srname") String srname);

    @Update("update `screenroom` set cid = #{cid},srname = #{srname} where srid = #{srid}")
    int updateScreenRoom(Screenroom screenroom);
}
