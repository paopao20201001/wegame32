package com.sichuan.poker.service;

import com.sichuan.poker.dto.CreateRoomDTO;
import com.sichuan.poker.dto.RoomInfoDTO;

import java.util.List;

/**
 * 房间服务接口
 */
public interface RoomService {

    /**
     * 创建房间
     */
    RoomInfoDTO createRoom(CreateRoomDTO createRoomDTO);

    /**
     * 获取房间列表
     */
    List<RoomInfoDTO> getRoomList(Integer maxPlayers, Integer page, Integer size);

    /**
     * 获取房间详情
     */
    RoomInfoDTO getRoomInfo(String roomId);

    /**
     * 加入房间
     */
    boolean joinRoom(String roomId, String openId);

    /**
     * 玩家准备
     */
    boolean playerReady(String roomId, String openId);

    /**
     * 离开房间
     */
    boolean leaveRoom(String roomId, String openId);

    /**
     * 解散房间
     */
    boolean disbandRoom(String roomId, String openId);
}