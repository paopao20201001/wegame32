package com.sichuan.poker.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sichuan.poker.dto.PlayCardDTO;
import com.sichuan.poker.entity.ApiResponse;
import com.sichuan.poker.entity.Room;
import com.sichuan.poker.repository.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏WebSocket处理器
 * 处理实时游戏通信
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private ObjectMapper objectMapper;

    // 房间WebSocket会话映射
    private static final Map<String, Map<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    // 玩家会话映射
    private static final Map<String, WebSocketSession> playerSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String openId = getPlayerIdFromSession(session);
        if (openId != null) {
            playerSessions.put(openId, session);
            System.out.println("玩家 " + openId + " 连接成功");
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            System.out.println("收到消息: " + payload);

            // 解析消息类型
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String messageType = (String) messageData.get("type");

            switch (messageType) {
                case "join_room":
                    handleJoinRoom(session, messageData);
                    break;
                case "leave_room":
                    handleLeaveRoom(session, messageData);
                    break;
                case "game_action":
                    handleGameAction(session, messageData);
                    break;
                case "heartbeat":
                    handleHeartbeat(session, messageData);
                    break;
                default:
                    sendErrorMessage(session, "未知的消息类型: " + messageType);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String openId = getPlayerIdFromSession(session);
        if (openId != null) {
            // 从所有房间中移除会话
            roomSessions.forEach((roomId, sessions) -> {
                sessions.remove(openId);
            });

            // 从玩家会话映射中移除
            playerSessions.remove(openId);
            System.out.println("玩家 " + openId + " 连接断开: " + status);
        }
    }

    /**
     * 处理加入房间
     */
    private void handleJoinRoom(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        String roomId = (String) messageData.get("roomId");
        String openId = getPlayerIdFromSession(session);

        if (roomId == null || openId == null) {
            sendErrorMessage(session, "房间ID或玩家ID不能为空");
            return;
        }

        // 获取房间信息
        Room room = roomMapper.findByRoomId(roomId);
        if (room == null) {
            sendErrorMessage(session, "房间不存在");
            return;
        }

        // 加入房间的WebSocket会话映射
        roomSessions.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                   .put(openId, session);

        // 通知房间内其他玩家
        notifyRoomPlayers(roomId, "player_joined", openId);

        // 发送加入成功消息
        sendMessage(session, buildSuccessMessage("joined_room", Map.of(
            "roomId", roomId,
            "players", getRoomPlayerIds(roomId)
        )));

        System.out.println("玩家 " + openId + " 加入房间 " + roomId);
    }

    /**
     * 处理离开房间
     */
    private void handleLeaveRoom(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        String roomId = (String) messageData.get("roomId");
        String openId = getPlayerIdFromSession(session);

        if (roomId == null || openId == null) {
            return;
        }

        // 从房间中移除会话
        Map<String, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            sessions.remove(openId);
        }

        // 通知房间内其他玩家
        notifyRoomPlayers(roomId, "player_left", openId);

        System.out.println("玩家 " + openId + " 离开房间 " + roomId);
    }

    /**
     * 处理游戏动作（出牌、过牌等）
     */
    private void handleGameAction(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        String roomId = (String) messageData.get("roomId");
        String openId = getPlayerIdFromSession(session);

        if (roomId == null || openId == null) {
            return;
        }

        // 广播游戏动作到房间内所有玩家
        notifyRoomPlayers(roomId, "game_action", messageData);

        System.out.println("房间 " + roomId + " 收到游戏动作: " + messageData);
    }

    /**
     * 处理心跳
     */
    private void handleHeartbeat(WebSocketSession session, Map<String, Object> messageData) throws IOException {
        String openId = getPlayerIdFromSession(session);
        if (openId != null) {
            sendMessage(session, buildSuccessMessage("heartbeat", Map.of(
                "timestamp", System.currentTimeMillis(),
                "playerId", openId
            )));
        }
    }

    /**
     * 通知房间内所有玩家
     */
    private void notifyRoomPlayers(String roomId, String eventType, Object eventData) throws IOException {
        Map<String, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            Map<String, Object> message = buildSuccessMessage(eventType, eventData);
            for (WebSocketSession session : sessions.values()) {
                if (session.isOpen()) {
                    sendMessage(session, message);
                }
            }
        }
    }

    /**
     * 发送消息
     */
    private void sendMessage(WebSocketSession session, Object message) throws IOException {
        if (session.isOpen()) {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }

    /**
     * 发送错误消息
     */
    private void sendErrorMessage(WebSocketSession session, String errorMessage) throws IOException {
        sendMessage(session, buildErrorMessage("error", errorMessage));
    }

    /**
     * 构建成功消息
     */
    private Map<String, Object> buildSuccessMessage(String type, Object data) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("success", true);
        message.put("data", data);
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * 构建错误消息
     */
    private Map<String, Object> buildErrorMessage(String type, String errorMessage) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("success", false);
        message.put("error", errorMessage);
        message.put("timestamp", System.currentTimeMillis());
        return message;
    }

    /**
     * 从会话中获取玩家ID
     */
    private String getPlayerIdFromSession(WebSocketSession session) {
        try {
            String query = session.getUri().getQuery();
            if (query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && "playerId".equals(keyValue[0])) {
                        return keyValue[1];
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取房间内所有玩家ID
     */
    private List<String> getRoomPlayerIds(String roomId) {
        Map<String, WebSocketSession> sessions = roomSessions.get(roomId);
        if (sessions != null) {
            return new ArrayList<>(sessions.keySet());
        }
        return new ArrayList<>();
    }

    /**
     * 获取房间WebSocket会话
     */
    public Map<String, WebSocketSession> getRoomSessions(String roomId) {
        return roomSessions.getOrDefault(roomId, new ConcurrentHashMap<>());
    }

    /**
     * 广播游戏状态
     */
    public void broadcastGameState(String roomId, String gameState) throws IOException {
        Map<String, Object> message = buildSuccessMessage("game_state", Map.of(
            "roomId", roomId,
            "state", gameState,
            "timestamp", System.currentTimeMillis()
        ));
        notifyRoomPlayers(roomId, "game_state", gameState);
    }
}