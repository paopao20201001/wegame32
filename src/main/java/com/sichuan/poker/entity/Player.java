package com.sichuan.poker.entity;

import lombok.Data;
import java.util.Date;

/**
 * 玩家实体
 */
@Data
public class Player {
    private Long id;
    private String openId;        // 微信OpenID
    private String unionId;       // 微信UnionID
    private String nickName;      // 昵称
    private String avatarUrl;     // 头像URL
    private Integer level;        // 等级
    private Integer score;        // 积分
    private Integer winCount;     // 胜利次数
    private Integer loseCount;    // 失败次数
    private Date lastLoginTime;
    private Integer status;       // 状态：0-离线，1-在线，2-游戏中
    private Boolean isRealNameAuth; // 是否实名认证
    private String realName;      // 真实姓名
    private String idCard;        // 身份证号
    private Date createTime;
    private Date updateTime;
}