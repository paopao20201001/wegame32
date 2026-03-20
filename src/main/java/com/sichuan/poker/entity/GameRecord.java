package com.sichuan.poker.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 游戏记录实体
 */
@Data
public class GameRecord {
    private Long id;
    private String gameId;        // 游戏ID
    private Long roomId;
    private Date startTime;
    private Date endTime;
    private Integer duration;      // 游戏时长（秒）
    private Integer status;       // 状态：0-未开始，1-进行中，2-已结束

    // 获胜者信息
    private Long winnerId;
    private String winnerName;
    private Integer winnerScore;

    // 结算信息（JSON格式）
    private String settlementInfo;

    // 玩家游戏记录
    private List<PlayerGameRecord> playerRecords;
}