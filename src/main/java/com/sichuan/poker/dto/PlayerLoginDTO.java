package com.sichuan.poker.dto;

import lombok.Data;

/**
 * 玩家登录DTO
 */
@Data
public class PlayerLoginDTO {
    private String code;          // 微信授权码
    private String nickName;      // 昵称
    private String avatarUrl;     // 头像URL
}