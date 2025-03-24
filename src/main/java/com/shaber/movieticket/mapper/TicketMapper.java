package com.shaber.movieticket.mapper;

import com.github.pagehelper.PageInfo;
import com.shaber.movieticket.dto.TicketDto;
import com.shaber.movieticket.pojo.Ticket;
import com.shaber.movieticket.pojo.TicketInfo;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface TicketMapper {
    // 模糊查询票
    List<TicketInfo> listTicketInfo(Map<String, Object> map);

    // 查询指定票
    Ticket getTicket(@Param("tid") String tid);

    // 查询该放映场次的座位是否存在票
    @Select("select * from ticket where sid = #{sid} and tseat = #{tseat}")
    Ticket getTicketBySidAndTseat(@Param("sid") String sid, @Param("tseat") String tseat);

    // 增加票的记录
    @Insert("insert into ticket (sid,tseat,tstatus) values (#{sid},#{tseat},#{tstatus})")
    int insertTicket(Ticket ticket);

    // 修改票的信息
    @Update("update ticket set sid = #{sid},tseat = #{tseat},tstatus = #{tstatus} where tid = #{tid}")
    int updateTicket(Ticket ticket);

    // 删除票的信息
    @Delete("delete from ticket where tid = #{tid}")
    int deleteTicket(@Param("tid") String tid);

    @Select("select * from ticket where sid = #{sid}")
    List<Ticket> findTicketsBySid(@Param("sid") String sid);

    @Update("update `ticket` set tstatus = #{i} where tid = #{tid}")
    int updateStatus(@Param("tid") String tid, @Param("i") int i);

    @Update("<script>" +
            "UPDATE ticket SET tstatus = #{status} WHERE tid IN " +
            "<foreach item='id' collection='tids' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("tids") List<String> tids,
                          @Param("status") int status);

    // 批量查询票务数据
    List<Ticket> findTicketsBySids(@Param("sids") List<String> sids);

    int batchDelete(List<String> tids);

    int batchDeleteBySids(@Param("sids") List<String> historySids);

    @Select("select * from `ticket` where sid = #{sid} and tseat = #{tseat}")
    Ticket findTicketBySidTseat(@Param("sid") String sid, @Param("tseat") String tseat);

    @Insert("insert into `ticket` values (#{tid},#{sid},#{tseat},#{tstatus})")
    int buildTicket(@Param("tid") String tid, @Param("sid") String sid,
                    @Param("tseat") String tseat, @Param("tstatus") int tstatus);

    @Select("select * from `ticket` where tid = #{tid}")
    Ticket findTicket(@Param("tid") String tid);

    List<TicketDto> selectTickets(@Param("uid") String uid,
                                      @Param("mname") String mname,
                                      @Param("cname") String cname,
                                      @Param("srname") String srname,
                                      @Param("sday") LocalDate sday);
}
