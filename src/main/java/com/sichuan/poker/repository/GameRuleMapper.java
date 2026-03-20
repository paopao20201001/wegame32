package com.sichuan.poker.repository;

import com.sichuan.poker.entity.GameRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 游戏规则数据访问层
 */
@Mapper
public interface GameRuleMapper {

    /**
     * 获取服务器默认规则
     */
    List<GameRule> getDefaultRules();

    /**
     * 获取房间自定义规则
     */
    List<GameRule> getRoomRules(@Param("roomId") String roomId);

    /**
     * 更新规则
     */
    int updateRules(@Param("roomId") String roomId, @Param("rules") List<GameRule> rules);

    /**
     * 插入规则
     */
    int insertRule(GameRule rule);
}