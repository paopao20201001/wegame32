package com.sichuan.poker.entity;

import lombok.Data;
import java.util.Date;

/**
 * 游戏规则配置实体
 */
@Data
public class GameRule {
    private Long id;
    private String ruleKey;        // 规键键
    private String ruleName;      // 规则名称
    private String ruleValue;     // 规则值
    private Integer ruleType;      // 规则类型：1-全服配置，2-房间配置
    private Long roomId;           // 房间ID（房间配置时使用）
    private Boolean isDefault;     // 是否是默认配置
    private Integer status;       // 状态：0-禁用，1-启用
    private String description;   // 规则描述
    private Date createTime;
    private Date updateTime;
}