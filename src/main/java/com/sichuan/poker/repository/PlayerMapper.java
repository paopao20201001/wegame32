package com.sichuan.poker.repository;

import com.sichuan.poker.entity.Player;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 玩家数据访问层
 */
@Mapper
public interface PlayerMapper {

    /**
     * 根据OpenID查询玩家
     */
    Player findByOpenId(@Param("openId") String openId);

    /**
     * 创建玩家
     */
    int insert(Player player);

    /**
     * 更新玩家信息
     */
    int update(Player player);

    /**
     * 更新玩家状态
     */
    int updateStatus(@Param("openId") String openId, @Param("status") Integer status);
}