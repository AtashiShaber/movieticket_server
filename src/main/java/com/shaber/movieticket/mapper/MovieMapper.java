package com.shaber.movieticket.mapper;

import com.shaber.movieticket.dto.MoviePreviewDto;
import com.shaber.movieticket.pojo.Movie;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface MovieMapper {
    // 模糊查询获取已经上映的电影
    List<Movie> listMovieRelese(Map<String, Object> map);

    // 模糊查询获取所有电影
    List<Movie> listMovie(Map<String, Object> map);

    // 通过mid指定查询电影
    @Select("select * from movie where mid = #{mid}")
    Movie getMovieByMid(String mid);

    // 增加电影
    @Insert("insert into movie (mid,mname,mp,mactor,mstarttime,mendtime) values (#{mid},#{mname},#{mp},#{mactor},#{mstarttime},#{mendtime})")
    int insertMovie(Map<String, Object> map);

    // 删除电影
    @Delete("delete from movie where mid = #{mid}")
    int deleteMovie(String mid);

    // 修改
    // 电影上映
    @Update("update `movie` set `mstatus` = 1 " +
            "where `mstarttime` <= #{today} " +
            "and `mendtime` >= #{today} " +
            "and `mstatus` = 0")
    int updateUpComing(LocalDate today);

    // 电影下映
    @Update("update `movie` set `mstatus` = 0 " +
            "where `mendtime` < #{today} and `mstatus` = 1")
    int updateDownCast(LocalDate today);

    // 电影信息修改
    @Update("update movie set mname = #{mname}, " +
            "mp = #{mp}, " +
            "mactor = #{mactor}, " +
            "mstatus = #{mstatus}, " +
            "mstarttime = #{mstarttime}, " +
            "mendtime = #{mendtime} " +
            "where mid = #{mid}")
    int updateMovie(@Param("mid") String mid,
                    @Param("mname") String mname,
                    @Param("mp") String mp,
                    @Param("mactor") String mactor,
                    @Param("mstatus") int mstatus,
                    @Param("mstarttime") LocalDate mstarttime,
                    @Param("mendtime") LocalDate mendtime);

    List<Movie> findUpcoming(@Param("now") LocalDate now);

    @Update("update `movie` set mstatus = #{i},mendtime = #{now} where mid = #{mid}")
    int downCast(@Param("mid") String mid, @Param("i") int i, @Param("now") LocalDate now);

    int upCast(@Param("mid") String mid, @Param("i") int i);

    @Update("update `movie` set mstatus = 1 where mendtime >= #{now} and mstarttime <= #{now} and mstatus = 0")
    int autoUpCast(@Param("now") LocalDate now);

    @Update("update `movie` set mstatus = 2 where mendtime < #{now} and mstatus <= 1")
    int autoDownCast(@Param("now") LocalDate now);

    @Select("select * from `movie` where mstatus = 1")
    List<Movie> listMoviesUpCase();

    List<MoviePreviewDto> findCast(@Param("now") LocalDate now);
}
