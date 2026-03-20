package com.sichuan.poker.service;

import com.sichuan.poker.dto.PlayCardDTO;

/**
 * 游戏服务接口
 */
public interface GameService {

    /**
     * 出牌
     */
    boolean playCard(PlayCardDTO playCardDTO);

    /**
     * 过牌
     */
    boolean pass(String roomId, String openId);

    /**
     * 获取游戏状态
     */
    Object getGameState(String roomId);

    /**
     * 获取游戏记录
     */
    Object getGameRecord(String gameId);

    /**
     * 计算游戏结果
     */
    Object calculateGameResult(String roomId);
}