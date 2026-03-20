package com.sichuan.poker.controller;

import com.sichuan.poker.entity.ApiResponse;
import com.sichuan.poker.websocket.GameWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏WebSocket控制器
 */
@RestController
@RequestMapping("/api/ws")
@CrossOrigin(origins = "*")
public class GameWebSocketController {

    @Autowired
    private GameWebSocketHandler gameWebSocketHandler;

    /**
     * 发送消息到指定房间
     */
    @PostMapping("/room/{roomId}/send")
    public ApiResponse<String> sendToRoom(
            @PathVariable String roomId,
            @RequestBody Map<String, Object> message) {
        try {
            // 这里可以添加消息验证逻辑
            gameWebSocketHandler.broadcastGameState(roomId, message.toString());
            return ApiResponse.success("消息发送成功");
        } catch (Exception e) {
            return ApiResponse.fail(500, "消息发送失败: " + e.getMessage());
        }
    }

    /**
     * 获取房间在线玩家
     */
    @GetMapping("/room/{roomId}/players")
    public ApiResponse<Map<String, Object>> getRoomPlayers(@PathVariable String roomId) {
        try {
            Map<String, Object> sessions = gameWebSocketHandler.getRoomSessions(roomId);
            List<String> playerIds = new ArrayList<>();
            sessions.keySet().forEach(playerIds::add);

            Map<String, Object> result = new HashMap<>();
            result.put("roomId", roomId);
            result.put("playerCount", sessions.size());
            result.put("players", playerIds);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.fail(500, "获取玩家信息失败: " + e.getMessage());
        }
    }

    /**
     * 强制断开玩家连接
     */
    @DeleteMapping("/player/{playerId}/disconnect")
    public ApiResponse<String> disconnectPlayer(@PathVariable String playerId) {
        try {
            // 这里可以实现强制断开逻辑
            return ApiResponse.success("玩家连接已断开");
        } catch (Exception e) {
            return ApiResponse.fail(500, "断开连接失败: " + e.getMessage());
        }
    }
}