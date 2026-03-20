package com.sichuan.poker.service;

import java.util.Map;

/**
 * 游戏规则引擎接口
 */
public interface RuleEngine {

    /**
     * 获取默认规则
     */
    Map<String, Object> getDefaultRules();

    /**
     * 验证规则合法性
     */
    boolean validateRules(Map<String, Object> rules);

    /**
     * 生成规则快照
     */
    Map<String, Object> generateRuleSnapshot(Map<String, Object> serverRules, Map<String, Object> customRules);

    /**
     * 检查牌型是否有效
     */
    boolean isValidPattern(Map<String, Object> cards, Map<String, Object> rules);

    /**
     * 比较两个牌型大小
     */
    int comparePatterns(Map<String, Object> pattern1, Map<String, Object> pattern2, Map<String, Object> rules);

    /**
     * 检查是否可以压过上家
     */
    boolean canBeatLastPlay(Map<String, Object> currentPlay, Map<String, Object> lastPlay, Map<String, Object> rules);

    /**
     * 检查强制规则
     */
    boolean checkForceRules(Map<String, Object> playData, Map<String, Object> rules);
}