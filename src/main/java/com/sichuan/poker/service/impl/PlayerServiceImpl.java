package com.sichuan.poker.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sichuan.poker.dto.PlayerInfoDTO;
import com.sichuan.poker.dto.PlayerLoginDTO;
import com.sichuan.poker.entity.Player;
import com.sichuan.poker.repository.PlayerMapper;
import com.sichuan.poker.service.PlayerService;
import com.sichuan.poker.utils.WxUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * 玩家服务实现
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    @Autowired
    private PlayerMapper playerMapper;

    @Autowired
    private WxUtils wxUtils;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public PlayerInfoDTO login(PlayerLoginDTO loginDTO) {
        try {
            // 1. 使用微信授权码获取openid
            String openId = wxUtils.getOpenId(loginDTO.getCode());
            if (openId == null) {
                throw new RuntimeException("微信授权失败");
            }

            // 2. 查询玩家是否存在
            Player player = playerMapper.findByOpenId(openId);
            if (player == null) {
                // 3. 创建新玩家
                player = new Player();
                player.setOpenId(openId);
                player.setNickName(loginDTO.getNickName());
                player.setAvatarUrl(loginDTO.getAvatarUrl());
                player.setLevel(1);
                player.setScore(1000);
                player.setWinCount(0);
                player.setLoseCount(0);
                player.setStatus(1);
                player.setCreateTime(new Date());
                player.setUpdateTime(new Date());

                playerMapper.insert(player);
            } else {
                // 4. 更新玩家信息
                player.setNickName(loginDTO.getNickName());
                player.setAvatarUrl(loginDTO.getAvatarUrl());
                player.setLastLoginTime(new Date());
                player.setUpdateTime(new Date());
                playerMapper.update(player);
            }

            // 5. 转换为DTO返回
            PlayerInfoDTO playerInfo = new PlayerInfoDTO();
            BeanUtils.copyProperties(player, playerInfo);
            playerInfo.setWinRate(player.getWinCount() + player.getLoseCount() > 0 ?
                    (double) player.getWinCount() / (player.getWinCount() + player.getLoseCount()) : 0.0);

            return playerInfo;

        } catch (Exception e) {
            throw new RuntimeException("登录失败: " + e.getMessage());
        }
    }

    @Override
    public PlayerInfoDTO getPlayerInfo(String openId) {
        Player player = playerMapper.findByOpenId(openId);
        if (player == null) {
            return null;
        }

        PlayerInfoDTO playerInfo = new PlayerInfoDTO();
        BeanUtils.copyProperties(player, playerInfo);
        playerInfo.setWinRate(player.getWinCount() + player.getLoseCount() > 0 ?
                (double) player.getWinCount() / (player.getWinCount() + player.getLoseCount()) : 0.0);

        return playerInfo;
    }

    @Override
    @Transactional
    public boolean updatePlayerInfo(String openId, PlayerInfoDTO playerInfo) {
        try {
            Player player = playerMapper.findByOpenId(openId);
            if (player == null) {
                return false;
            }

            // 只允许更新非关键字段
            player.setNickName(playerInfo.getNickName());
            player.setAvatarUrl(playerInfo.getAvatarUrl());
            player.setUpdateTime(new Date());

            return playerMapper.update(player) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean realNameAuth(String openId, String realName, String idCard) {
        try {
            Player player = playerMapper.findByOpenId(openId);
            if (player == null) {
                return false;
            }

            // 这里应该调用实名认证接口验证
            // 简化处理，直接更新状态
            player.setRealName(realName);
            player.setIdCard(idCard);
            player.setIsRealNameAuth(true);
            player.setUpdateTime(new Date());

            return playerMapper.update(player) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}