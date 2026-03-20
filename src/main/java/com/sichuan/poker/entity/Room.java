package com.sichuan.poker.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 游戏房间实体
 */
@Data
public class Room {
    private Long id;
    private String roomId;         // 房间唯一标识
    private String roomName;       // 房间名称
    private Long ownerId;          // 房主ID
    private Integer playerCount;   // 玩家数量
    private Integer maxPlayers;    // 最大玩家数
    private Integer status;        // 状态：0-等待中，1-游戏中，2-已结束
    private String password;       // 房间密码（可选）
    private Boolean isPrivate;     // 是否私有房间
    private Date createTime;
    private Date startTime;
    private Date endTime;

    // 规则快照（JSON格式存储）
    private String rulesSnapshot;

    // 玩家列表
    private List<Player> players;
}