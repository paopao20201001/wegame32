package com.sichuan.poker.entity;

import lombok.Data;
import java.util.Date;

/**
 * 玩家游戏记录实体
 */
@Data
public class PlayerGameRecord {
    private Long id;
    private Long gameRecordId;
    private Long playerId;
    private String playerName;
    private Integer initialScore;  // 初始分数
    private Integer finalScore;    // 最终分数
    private Integer scoreChange;   // 分数变化
    private Integer cardCount;     // 剩余牌数
    private Integer rank;          // 排名
    private Boolean isWinner;      // 是否获胜
    private Date createTime;
}