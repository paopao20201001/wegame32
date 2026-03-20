package com.sichuan.poker.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sichuan.poker.service.RuleEngine;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 游戏规则引擎实现
 */
@Service
public class RuleEngineImpl implements RuleEngine {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> getDefaultRules() {
        Map<String, Object> rules = new HashMap<>();

        // 基础规则
        rules.put("card_count", 54);                    // 牌数：52/54
        rules.put("cards_per_player", 17);              // 每人牌数
        rules.put("first_hand_rule", "red_heart_4");    // 先手规则
        rules.put("play_order", "clockwise");           // 出牌顺序
        rules.put("play_time_limit", 30);               // 出牌时间限制（秒）

        // 出牌类型配置
        rules.put("enable_single", true);               // 单张
        rules.put("enable_pair", true);                 // 对子
        rules.put("enable_three", true);                // 三张
        rules.put("enable_three_with_two", true);       // 3带2
        rules.put("enable_straight", true);            // 顺子
        rules.put("enable_consecutive_pair", true);     // 连对
        rules.put("enable_airplane", true);             // 飞机
        rules.put("enable_bomb", true);                // 炸弹
        rules.put("enable_royal_bomb", true);          // 王炸

        // 强制规则
        rules.put("force_4444", false);                // 4444强制出牌
        rules.put("force_bomb_response", false);       // 炸弹强制响应
        rules.put("first_must_red_heart_4", false);    // 首出必含红桃4

        // 压制规则
        rules.put("bomb_suppress_range", "all");       // 炸弹压制范围

        // 计分规则
        rules.put("base_score", 10);                   // 基础分
        rules.put("bomb_multiplier", 2);               // 炸弹倍数
        rules.put("win_streak_bonus", 0.1);            // 连胜加成
        rules.put("remaining_card_penalty", 0.5);      // 剩余牌惩罚系数

        return rules;
    }

    @Override
    public boolean validateRules(Map<String, Object> rules) {
        try {
            // 验证基础规则
            if (rules.get("card_count") != null &&
                !Arrays.asList(52, 54).contains(rules.get("card_count"))) {
                return false;
            }

            if (rules.get("cards_per_player") != null) {
                int cardsPerPlayer = (Integer) rules.get("cards_per_player");
                if (cardsPerPlayer < 10 || cardsPerPlayer > 20) {
                    return false;
                }
            }

            if (rules.get("play_time_limit") != null) {
                int timeLimit = (Integer) rules.get("play_time_limit");
                if (timeLimit < 10 || timeLimit > 60) {
                    return false;
                }
            }

            // 验证至少有一种牌型启用
            boolean hasEnabledPattern = false;
            if (rules.containsKey("enable_single") && (boolean) rules.get("enable_single")) {
                hasEnabledPattern = true;
            } else if (rules.containsKey("enable_pair") && (boolean) rules.get("enable_pair")) {
                hasEnabledPattern = true;
            } else if (rules.containsKey("enable_three") && (boolean) rules.get("enable_three")) {
                hasEnabledPattern = true;
            } else if (rules.containsKey("enable_three_with_two") && (boolean) rules.get("enable_three_with_two")) {
                hasEnabledPattern = true;
            } else if (rules.containsKey("enable_straight") && (boolean) rules.get("enable_straight")) {
                hasEnabledPattern = true;
            } else if (rules.containsKey("enable_consecutive_pair") && (boolean) rules.get("enable_consecutive_pair")) {
                hasEnabledPattern = true;
            } else if (rules.containsKey("enable_airplane") && (boolean) rules.get("enable_airplane")) {
                hasEnabledPattern = true;
            } else if (rules.containsKey("enable_bomb") && (boolean) rules.get("enable_bomb")) {
                hasEnabledPattern = true;
            } else if (rules.containsKey("enable_royal_bomb") && (boolean) rules.get("enable_royal_bomb")) {
                hasEnabledPattern = true;
            }

            return hasEnabledPattern;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Map<String, Object> generateRuleSnapshot(Map<String, Object> serverRules, Map<String, Object> customRules) {
        Map<String, Object> snapshot = new HashMap<>();

        // 复制服务器默认规则
        snapshot.putAll(serverRules);

        // 应用自定义规则覆盖
        if (customRules != null) {
            snapshot.putAll(customRules);
        }

        return snapshot;
    }

    @Override
    public boolean isValidPattern(Map<String, Object> cards, Map<String, Object> rules) {
        // 这里需要实现具体的牌型识别逻辑
        // 简化实现，实际应该根据牌的规则进行判断
        try {
            List<Map<String, String>> cardList = (List<Map<String, String>>) cards.get("cards");
            if (cardList == null || cardList.isEmpty()) {
                return false;
            }

            int cardCount = cardList.size();

            // 根据牌数和规则判断可能的牌型
            if (cardCount == 1) {
                return rules.containsKey("enable_single") && (boolean) rules.get("enable_single");
            } else if (cardCount == 2) {
                // 判断是否是对子或王炸
                if (cardList.get(0).get("rank").equals(cardList.get(1).get("rank"))) {
                    return rules.containsKey("enable_pair") && (boolean) rules.get("enable_pair");
                }
                // 判断是否是王炸
                if (cardList.get(0).get("suit").equals("BJ") && cardList.get(1).get("suit").equals("RJ") ||
                    cardList.get(0).get("suit").equals("RJ") && cardList.get(1).get("suit").equals("BJ")) {
                    return rules.containsKey("enable_royal_bomb") && (boolean) rules.get("enable_royal_bomb");
                }
            } else if (cardCount == 3) {
                // 三张
                String rank = cardList.get(0).get("rank");
                boolean isThree = cardList.stream().allMatch(card -> card.get("rank").equals(rank));
                return isThree && rules.containsKey("enable_three") && (boolean) rules.get("enable_three");
            }
            // 更多牌型的判断...

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int comparePatterns(Map<String, Object> pattern1, Map<String, Object> pattern2, Map<String, Object> rules) {
        // 实现牌型大小比较逻辑
        String type1 = (String) pattern1.get("type");
        String type2 = (String) pattern2.get("type");

        // 炸弹最大
        if ("BOMB".equals(type1) && !"BOMB".equals(type2)) return 1;
        if (!"BOMB".equals(type1) && "BOMB".equals(type2)) return -1;
        if ("BOMB".equals(type1) && "BOMB".equals(type2)) {
            // 比较炸弹大小
            return ((Integer) pattern1.get("mainValue")).compareTo((Integer) pattern2.get("mainValue"));
        }

        // 王炸最大
        if ("ROYAL_BOMB".equals(type1)) return 1;
        if ("ROYAL_BOMB".equals(type2)) return -1;

        // 同类型牌比较
        if (type1.equals(type2)) {
            return ((Integer) pattern1.get("mainValue")).compareTo((Integer) pattern2.get("mainValue"));
        }

        // 不同类型牌，根据规则判断
        return 0;
    }

    @Override
    public boolean canBeatLastPlay(Map<String, Object> currentPlay, Map<String, Object> lastPlay, Map<String, Object> rules) {
        if (lastPlay == null || lastPlay.isEmpty()) {
            return true; // 首出可以出任意牌
        }

        // 如果上家过牌，可以出任意牌型
        if ("PASS".equals(lastPlay.get("action"))) {
            return true;
        }

        return comparePatterns(currentPlay, lastPlay, rules) > 0;
    }

    @Override
    public boolean checkForceRules(Map<String, Object> playData, Map<String, Object> rules) {
        // 检查首出必含红桃4
        if (rules.containsKey("first_must_red_heart_4") &&
            (boolean) rules.get("first_must_red_heart_4")) {
            // 需要检查是否是首出且包含红桃4
        }

        // 检查4444强制出牌
        if (rules.containsKey("force_4444") && (boolean) rules.get("force_4444")) {
            // 检查是否有4个4且没有出
        }

        // 检查炸弹强制响应
        if (rules.containsKey("force_bomb_response") &&
            (boolean) rules.get("force_bomb_response")) {
            // 检查上家是否出炸弹且当前玩家有炸弹但没有出
        }

        return true;
    }
}