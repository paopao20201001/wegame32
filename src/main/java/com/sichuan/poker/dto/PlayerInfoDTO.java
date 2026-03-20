package com.sichuan.poker.dto;

import lombok.Data;
import java.util.Date;

/**
 * 玩家信息DTO
 */
@Data
public class PlayerInfoDTO {
    private Long id;
    private String openId;
    private String nickName;
    private String avatarUrl;
    private Integer level;
    private Integer score;
    private Integer winCount;
    private Integer loseCount;
    private Double winRate;        // 胜率
    private Date lastLoginTime;
    private Boolean isRealNameAuth;
}