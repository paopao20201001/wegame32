package com.sichuan.poker.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 房间信息DTO
 */
@Data
public class RoomInfoDTO {
    private String roomId;
    private String roomName;
    private Long ownerId;
    private String ownerName;
    private Integer playerCount;
    private Integer maxPlayers;
    private Integer status;
    private Boolean isPrivate;
    private Map<String, Object> rules;
    private List<PlayerInfoDTO> players;
    private Boolean canJoin;       // 当前用户是否可以加入
}