package com.sichuan.poker.service;

import com.sichuan.poker.exception.BusinessException;
import com.sichuan.poker.exception.NetworkException;
import com.sichuan.poker.repository.RoomMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 断线重连服务
 */
@Service
public class ReconnectionService {

    private static final Logger logger = LoggerFactory.getLogger(ReconnectionService.class);

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private CacheService cacheService;

    // 断线重连任务池
    private static final ScheduledExecutorService reconnectionPool = Executors.newScheduledThreadPool(5);

    // 玩家断线记录
    private static final ConcurrentHashMap<String, Long> playerOfflineRecords = new ConcurrentHashMap<>();

    // 是否正在重连
    private static final ConcurrentHashMap<String, Boolean> reconnectingPlayers = new ConcurrentHashMap<>();

    /**
     * 玩家断线处理
     */
    public void handlePlayerOffline(String roomId, String playerId) {
        logger.info("玩家 {} 断线，房间: {}", playerId, roomId);

        // 记录断线时间
        playerOfflineRecords.put(playerId, System.currentTimeMillis());

        // 1. 设置玩家为离线状态
        cacheService.setPlayerOnline(roomId, playerId, false);

        // 2. 检查是否需要重连
        scheduleReconnection(roomId, playerId);

        // 3. 通知房间内其他玩家
        notifyRoomOffline(roomId, playerId);
    }

    /**
     * 玩家重连处理
     */
    public void handlePlayerReconnect(String roomId, String playerId) {
        logger.info("玩家 {} 重连，房间: {}", playerId, roomId);

        // 1. 清除重连记录
        playerOfflineRecords.remove(playerId);
        reconnectingPlayers.remove(playerId);

        // 2. 设置玩家为在线状态
        cacheService.setPlayerOnline(roomId, playerId, true);

        // 3. 更新游戏状态
        updateGameOnReconnect(roomId, playerId);

        // 4. 通知房间内其他玩家
        notifyRoomReconnect(roomId, playerId);
    }

    /**
     * 安排重连任务
     */
    private void scheduleReconnection(String roomId, String playerId) {
        // 防止重复重连
        if (reconnectingPlayers.containsKey(playerId)) {
            return;
        }

        reconnectingPlayers.put(playerId, true);

        // 30秒后尝试重连
        reconnectionPool.schedule(() -> {
            try {
                performReconnection(roomId, playerId);
            } catch (Exception e) {
                logger.error("重连失败，玩家: {}, 房间: {}", playerId, roomId, e);
                // 重连失败，再次安排
                if (!playerOfflineRecords.containsKey(playerId)) {
                    scheduleReconnection(roomId, playerId);
                }
            }
        }, 30, TimeUnit.SECONDS);
    }

    /**
     * 执行重连
     */
    private void performReconnection(String roomId, String playerId) {
        // 检查房间是否还存在
        if (roomMapper.findByRoomId(roomId) == null) {
            logger.info("房间 {} 已不存在，跳过重连", roomId);
            playerOfflineRecords.remove(playerId);
            reconnectingPlayers.remove(playerId);
            return;
        }

        // 检查玩家是否已经重连
        if (!cacheService.getPlayerOnline(roomId, playerId)) {
            // 执行重连逻辑
            handlePlayerReconnect(roomId, playerId);
        }
    }

    /**
     * 更新重连后的游戏状态
     */
    private void updateGameOnReconnect(String roomId, String playerId) {
        try {
            // 获取当前游戏状态
            Object gameState = cacheService.getGameStateCache(roomId);
            if (gameState == null) {
                // 游戏未开始，玩家可以正常加入
                cacheService.setGameStateCache(roomId, "waiting", 30, TimeUnit.MINUTES);
                return;
            }

            // 根据游戏状态进行相应处理
            String gameStateStr = (String) gameState;
            switch (gameStateStr) {
                case "waiting":
                    // 等待中，玩家可以加入
                    break;
                case "playing":
                    // 游戏进行中，检查玩家状态
                    handleReconnectDuringGame(roomId, playerId);
                    break;
                case "ended":
                    // 游戏已结束，玩家观战
                    break;
            }

        } catch (Exception e) {
            logger.error("更新重连后游戏状态失败，房间: {}, 玩家: {}", roomId, playerId, e);
            throw new BusinessException("重连后状态更新失败");
        }
    }

    /**
     * 处理游戏进行中的重连
     */
    private void handleReconnectDuringGame(String roomId, String playerId) {
        // 1. 检查玩家是否还需要继续游戏
        // 这里可以实现更复杂的逻辑，比如询问玩家是否继续

        // 2. 检查游戏是否可以继续
        Long playerCount = cacheService.getRoomPlayerCount(roomId);
        if (playerCount < 2) {
            // 玩家数量不足，结束游戏
            endGameForInsufficientPlayers(roomId);
            return;
        }

        // 3. 玩家继续游戏
        logger.info("玩家 {} 继续游戏，房间: {}", playerId, roomId);
    }

    /**
     * 由于玩家不足结束游戏
     */
    private void endGameForInsufficientPlayers(String roomId) {
        try {
            // 更新房间状态
            // 这里可以调用房间服务来结束游戏
            logger.info("房间 {} 玩家不足，游戏结束", roomId);

            // 清除游戏状态缓存
            cacheService.evictGameStateCache(roomId);

            // 通知所有玩家游戏结束
            notifyGameEnd(roomId);

        } catch (Exception e) {
            logger.error("结束游戏失败，房间: {}", roomId, e);
            throw new BusinessException("结束游戏失败");
        }
    }

    /**
     * 通知玩家断线
     */
    private void notifyRoomOffline(String roomId, String playerId) {
        // 通过WebSocket通知房间内其他玩家
        // 这里可以调用WebSocket服务发送消息
        logger.info("通知房间 {} 玩家 {} 断线", roomId, playerId);
    }

    /**
     * 通知玩家重连
     */
    private void notifyRoomReconnect(String roomId, String playerId) {
        // 通过WebSocket通知房间内其他玩家
        // 这里可以调用WebSocket服务发送消息
        logger.info("通知房间 {} 玩家 {} 重连", roomId, playerId);
    }

    /**
     * 通知游戏结束
     */
    private void notifyGameEnd(String roomId) {
        // 通过WebSocket通知房间内所有玩家
        // 这里可以调用WebSocket服务发送消息
        logger.info("通知房间 {} 游戏结束", roomId);
    }

    /**
     * 获取玩家断线时间
     */
    public Long getPlayerOfflineTime(String playerId) {
        return playerOfflineRecords.get(playerId);
    }

    /**
     * 检查玩家是否正在重连
     */
    public boolean isPlayerReconnecting(String playerId) {
        return reconnectingPlayers.getOrDefault(playerId, false);
    }

    /**
     * 玩家主动离开（非断线）
     */
    public void handlePlayerLeave(String roomId, String playerId) {
        // 清除所有重连相关记录
        playerOfflineRecords.remove(playerId);
        reconnectingPlayers.remove(playerId);

        // 更新房间玩家数量
        cacheService.decrementRoomPlayerCount(roomId);

        // 通知房间内其他玩家
        notifyRoomOffline(roomId, playerId);
    }

    /**
     * 清理过期断线记录
     */
    @Async
    public void cleanupOfflineRecords() {
        long now = System.currentTimeMillis();
        long expireTime = 30 * 60 * 1000; // 30分钟过期

        playerOfflineRecords.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > expireTime) {
                logger.info("清理过期的断线记录，玩家: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }
}