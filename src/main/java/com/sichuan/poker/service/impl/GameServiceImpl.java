package com.sichuan.poker.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sichuan.poker.dto.PlayCardDTO;
import com.sichuan.poker.entity.GameRecord;
import com.sichuan.poker.entity.Player;
import com.sichuan.poker.entity.Room;
import com.sichuan.poker.repository.RoomMapper;
import com.sichuan.poker.service.GameService;
import com.sichuan.poker.service.RuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏服务实现
 */
@Service
public class GameServiceImpl implements GameService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RuleEngine ruleEngine;

    // 游戏状态缓存
    private static final Map<String, Object> gameStateCache = new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean playCard(PlayCardDTO playCardDTO) {
        try {
            // 1. 获取房间规则
            Room room = roomMapper.findByRoomId(playCardDTO.getRoomId());
            if (room == null) {
                return false;
            }

            // 2. 解析规则快照
            @SuppressWarnings("unchecked")
            Map<String, Object> rules = objectMapper.readValue(room.getRulesSnapshot(), Map.class);

            // 3. 验证出牌
            if (!validatePlay(playCardDTO, rules)) {
                return false;
            }

            // 4. 更新游戏状态
            updateGameState(playCardDTO, room);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean pass(String roomId, String openId) {
        try {
            // 1. 获取游戏状态
            Object gameState = gameStateCache.get(roomId);
            if (gameState == null) {
                return false;
            }

            // 2. 更新为过牌
            @SuppressWarnings("unchecked")
            Map<String, Object> state = (Map<String, Object>) gameState;
            state.put("lastAction", "PASS");
            state.put("lastPlayer", openId);
            state.put("passCount", (Integer) state.getOrDefault("passCount", 0) + 1);

            // 3. 检查是否连续两人过牌
            if ((Integer) state.get("passCount") >= 2) {
                // 清空桌面
                state.put("lastPlay", null);
                state.put("passCount", 0);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Object getGameState(String roomId) {
        // 1. 从缓存获取
        Object gameState = gameStateCache.get(roomId);
        if (gameState == null) {
            // 2. 初始化游戏状态
            gameState = initializeGameState(roomId);
            gameStateCache.put(roomId, gameState);
        }

        return gameState;
    }

    @Override
    public Object getGameRecord(String gameId) {
        // 这里应该从数据库查询游戏记录
        // 简化处理，返回null
        return null;
    }

    @Override
    public Object calculateGameResult(String roomId) {
        try {
            // 1. 获取游戏状态
            Object gameState = gameStateCache.get(roomId);
            if (gameState == null) {
                return null;
            }

            // 2. 判断获胜者
            @SuppressWarnings("unchecked")
            Map<String, Object> state = (Map<String, Object>) gameState;
            String winnerId = (String) state.get("winner");

            // 3. 计算分数
            @SuppressWarnings("unchecked")
            Map<String, Object> rules = (Map<String, Object>) state.get("rules");
            int baseScore = (Integer) rules.get("base_score");
            int bombMultiplier = (Integer) rules.get("bomb_multiplier");

            // 4. 创建游戏记录
            GameRecord gameRecord = new GameRecord();
            gameRecord.setGameId("GAME_" + System.currentTimeMillis());
            gameRecord.setRoomId(Long.parseLong(roomId));
            gameRecord.setWinnerId(Long.parseLong(winnerId));
            gameRecord.setSettlementInfo(objectMapper.valueToTree(createSettlementInfo(state)).toString());

            // 5. 更新玩家积分
            updatePlayerScores(roomId, state);

            return gameRecord;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证出牌
     */
    private boolean validatePlay(PlayCardDTO playCardDTO, Map<String, Object> rules) {
        // 1. 检查是否轮到该玩家出牌
        Object gameState = gameStateCache.get(playCardDTO.getRoomId());
        @SuppressWarnings("unchecked")
        Map<String, Object> state = (Map<String, Object>) gameState;
        String currentPlayer = (String) state.get("currentPlayer");
        if (!currentPlayer.equals(playCardDTO.getPlayerId().toString())) {
            return false;
        }

        // 2. 检查牌型是否有效
        Map<String, Object> cardData = new HashMap<>();
        cardData.put("cards", playCardDTO.getCardIds());
        if (!ruleEngine.isValidPattern(cardData, rules)) {
            return false;
        }

        // 3. 检查是否可以压过上家
        Object lastPlay = state.get("lastPlay");
        if (lastPlay != null && !ruleEngine.canBeatLastPlay(cardData, (Map<String, Object>) lastPlay, rules)) {
            return false;
        }

        // 4. 检查强制规则
        Map<String, Object> playData = new HashMap<>();
        playData.put("cards", playCardDTO.getCardIds());
        playData.put("rules", rules);
        if (!ruleEngine.checkForceRules(playData, rules)) {
            return false;
        }

        return true;
    }

    /**
     * 更新游戏状态
     */
    private void updateGameState(PlayCardDTO playCardDTO, Room room) {
        Object gameState = gameStateCache.get(playCardDTO.getRoomId());
        @SuppressWarnings("unchecked")
        Map<String, Object> state = (Map<String, Object>) gameState;

        // 1. 更新最后出牌
        Map<String, Object> play = new HashMap<>();
        play.put("playerId", playCardDTO.getPlayerId());
        play.put("cards", playCardDTO.getCardIds());
        play.put("timestamp", System.currentTimeMillis());
        state.put("lastPlay", play);

        // 2. 重置过牌计数
        state.put("passCount", 0);

        // 3. 切换到下一个玩家
        List<String> players = (List<String>) state.get("players");
        int currentIndex = players.indexOf(playCardDTO.getPlayerId().toString());
        int nextIndex = (currentIndex + 1) % players.size();
        state.put("currentPlayer", players.get(nextIndex));

        // 4. 检查是否获胜
        checkWinner(state, room);
    }

    /**
     * 初始化游戏状态
     */
    private Object initializeGameState(String roomId) {
        Map<String, Object> gameState = new HashMap<>();

        // 1. 基础信息
        gameState.put("roomId", roomId);
        gameState.put("status", "PLAYING");
        gameState.put("startTime", System.currentTimeMillis());

        // 2. 玩家列表
        List<Player> players = roomMapper.getRoomPlayers(roomId);
        List<String> playerIds = new ArrayList<>();
        for (Player player : players) {
            playerIds.add(player.getId().toString());
        }
        gameState.put("players", playerIds);

        // 3. 游戏规则
        Room room = roomMapper.findByRoomId(roomId);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> rules = objectMapper.readValue(room.getRulesSnapshot(), Map.class);
            gameState.put("rules", rules);
        } catch (Exception e) {
            gameState.put("rules", ruleEngine.getDefaultRules());
        }

        // 4. 游戏状态
        gameState.put("currentPlayer", playerIds.get(0)); // 第一个玩家先出
        gameState.put("lastPlay", null);
        gameState.put("passCount", 0);

        return gameState;
    }

    /**
     * 检查获胜者
     */
    private void checkWinner(Map<String, Object> state, Room room) {
        // 这里应该检查每个玩家的手牌数量
        // 简化处理，暂时不实现
    }

    /**
     * 创建结算信息
     */
    private Map<String, Object> createSettlementInfo(Map<String, Object> state) {
        Map<String, Object> settlement = new HashMap<>();
        settlement.put("winner", state.get("winner"));
        settlement.put("duration", System.currentTimeMillis() - (Long) state.get("startTime"));
        // 添加更多结算信息...
        return settlement;
    }

    /**
     * 更新玩家积分
     */
    private void updatePlayerScores(String roomId, Map<String, Object> state) {
        // 这里应该更新玩家的积分记录
        // 简化处理，暂时不实现
    }
}