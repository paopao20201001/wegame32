package com.sichuan.poker.dto;

import lombok.Data;
import java.util.Map;

/**
 * 创建房间DTO
 */
@Data
public class CreateRoomDTO {
    private String roomName;      // 房间名称
    private String password;      // 房间密码（可选）
    private Boolean isPrivate;    // 是否私有房间
    private Integer maxPlayers;    // 最大玩家数
    private Map<String, Object> rules;  // 自定义规则
}