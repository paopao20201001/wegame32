package com.sichuan.poker.entity;

import lombok.Data;
import java.util.Date;

/**
 * 房间玩家关联实体
 */
@Data
public class RoomPlayer {
    private Long id;
    private Long roomId;
    private Long playerId;
    private Integer seatIndex;     // 座位索引
    private Integer status;       // 状态：0-未准备，1-已准备，2-游戏中
    private Boolean isOwner;       // 是否房主
    private Date createTime;
    private Date readyTime;
}