package com.shaber.movieticket.mapper;

import com.shaber.movieticket.dto.ScreeningDto;
import com.shaber.movieticket.pojo.Screening;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ScreeningMapper {

    // 查询指定电影指定电影院的放映信息
    List<Screening> listScreeningTarget(Map<String, Object> map);

    // 通过指定日期和场次时间安排查询放映
    List<Screening> listScreeningByDate(@Param("sday") LocalDate sday, @Param("stime") String stime);

    // 通过sid查询指定放映记录
    Screening getScreening(@Param("sid") String sid);

    // 添加放映场次
    int insertScreening(Screening screening);

    // 修改放映记录信息
    int updateScreening(Screening screening);

    // 删除放映记录
    int deleteScreening(@Param("sid") String sid);

    @Select("select * from `screening` where mid = #{mid}")
    List<Screening> findScreeningsByMid(@Param("mid") String mid);

    @Delete("<script>" +
            "DELETE FROM screening WHERE sid IN " +
            "<foreach item='id' collection='sids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDelete(@Param("sids") List<String> sids);

    @Select("select * from `screening` where srid = #{srid}")
    List<Screening> findScreeningsBySrid(@Param("srid") String srid);

    List<Screening> selectBySrids(@Param("srids") List<String> srids);

    List<ScreeningDto> listSreening(@Param("mname") String mnmae,
                                    @Param("srname") String srname,
                                    @Param("cname") String cname,
                                    @Param("sday") LocalDate sday,
                                    @Param("stime") String stime);

    @Select("select * from `screening` where srid = #{srid} and sday = #{sday} and stime = #{stime}")
    Screening findScreeningBySridDate(@Param("srid") String srid, @Param("sday") LocalDate sday, @Param("stime") String stime);

    @Select("select * from `screening` where srid = #{srid} and sday = #{sday}")
    List<Screening> findScreeningByCid(@Param("srid") String srid, @Param("sday") LocalDate sday);

    @Select("select count(1) from `screening` where sday = CURDATE()")
    Integer countToday();
}
