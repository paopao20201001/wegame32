package com.sichuan.poker.repository;

import com.sichuan.poker.entity.Room;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 房间数据访问层
 */
@Mapper
public interface RoomMapper {

    /**
     * 创建房间
     */
    int insert(Room room);

    /**
     * 根据roomId查询房间
     */
    Room findByRoomId(@Param("roomId") String roomId);

    /**
     * 更新房间状态
     */
    int updateStatus(@Param("roomId") String roomId, @Param("status") Integer status);

    /**
     * 查询等待中的房间
     */
    List<Room> findWaitingRooms(@Param("maxPlayers") Integer maxPlayers);

    /**
     * 加入房间
     */
    int joinRoom(@Param("roomId") String roomId, @Param("openId") String openId);

    /**
     * 玩家准备
     */
    int playerReady(@Param("roomId") String roomId, @Param("openId") String openId);

    /**
     * 查询房间玩家列表
     */
    List<Player> getRoomPlayers(@Param("roomId") String roomId);

    /**
     * 删除房间
     */
    int deleteByRoomId(@Param("roomId") String roomId);
}