package com.sichuan.poker.service;

import com.sichuan.poker.dto.PlayerInfoDTO;
import com.sichuan.poker.dto.PlayerLoginDTO;

/**
 * 玩家服务接口
 */
public interface PlayerService {

    /**
     * 微信登录
     */
    PlayerInfoDTO login(PlayerLoginDTO loginDTO);

    /**
     * 获取玩家信息
     */
    PlayerInfoDTO getPlayerInfo(String openId);

    /**
     * 更新玩家信息
     */
    boolean updatePlayerInfo(String openId, PlayerInfoDTO playerInfo);

    /**
     * 实名认证
     */
    boolean realNameAuth(String openId, String realName, String idCard);
}