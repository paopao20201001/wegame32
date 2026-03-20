package com.sichuan.poker.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sichuan.poker.dto.CreateRoomDTO;
import com.sichuan.poker.dto.PlayerInfoDTO;
import com.sichuan.poker.dto.RoomInfoDTO;
import com.sichuan.poker.entity.Player;
import com.sichuan.poker.entity.Room;
import com.sichuan.poker.repository.RoomMapper;
import com.sichuan.poker.service.PlayerService;
import com.sichuan.poker.service.RoomService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 房间服务实现
 */
@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private PlayerService playerService;

    // 房间缓存
    private static final Map<String, Room> roomCache = new ConcurrentHashMap<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public RoomInfoDTO createRoom(CreateRoomDTO createRoomDTO) {
        // 1. 生成房间ID
        String roomId = generateRoomId();

        // 2. 创建房间实体
        Room room = new Room();
        room.setRoomId(roomId);
        room.setRoomName(createRoomDTO.getRoomName());
        room.setMaxPlayers(createRoomDTO.getMaxPlayers());
        room.setStatus(0); // 等待中
        room.setIsPrivate(createRoomDTO.getIsPrivate());
        room.setPassword(createRoomDTO.getPassword());
        room.setCreateTime(new Date());

        // 3. 生成规则快照
        Map<String, Object> defaultRules = getDefaultRules();
        if (createRoomDTO.getRules() != null) {
            defaultRules.putAll(createRoomDTO.getRules());
        }
        room.setRulesSnapshot(objectMapper.valueToTree(defaultRules).toString());

        // 4. 保存到数据库
        roomMapper.insert(room);

        // 5. 加入缓存
        roomCache.put(roomId, room);

        // 6. 转换为DTO返回
        return convertToRoomInfoDTO(room);
    }

    @Override
    public List<RoomInfoDTO> getRoomList(Integer maxPlayers, Integer page, Integer size) {
        List<RoomInfoDTO> roomList = new ArrayList<>();
        List<Room> rooms = roomMapper.findWaitingRooms(maxPlayers);

        for (Room room : rooms) {
            roomList.add(convertToRoomInfoDTO(room));
        }

        return roomList;
    }

    @Override
    public RoomInfoDTO getRoomInfo(String roomId) {
        // 1. 从缓存获取
        Room room = roomCache.get(roomId);
        if (room == null) {
            // 2. 从数据库获取
            room = roomMapper.findByRoomId(roomId);
            if (room != null) {
                roomCache.put(roomId, room);
            }
        }

        if (room == null) {
            return null;
        }

        return convertToRoomInfoDTO(room);
    }

    @Override
    @Transactional
    public boolean joinRoom(String roomId, String openId) {
        try {
            // 1. 获取房间信息
            Room room = roomMapper.findByRoomId(roomId);
            if (room == null) {
                return false;
            }

            // 2. 检查房间状态
            if (room.getStatus() != 0) {
                return false;
            }

            // 3. 检查玩家数量
            if (room.getPlayerCount() >= room.getMaxPlayers()) {
                return false;
            }

            // 4. 加入房间
            int result = roomMapper.joinRoom(roomId, openId);
            if (result > 0) {
                // 更新房间缓存
                room.setPlayerCount(room.getPlayerCount() + 1);
                roomCache.put(roomId, room);
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean playerReady(String roomId, String openId) {
        try {
            // 1. 获取房间信息
            Room room = roomMapper.findByRoomId(roomId);
            if (room == null) {
                return false;
            }

            // 2. 玩家准备
            int result = roomMapper.playerReady(roomId, openId);
            if (result > 0) {
                // 检查是否所有玩家都准备好了
                int playerCount = room.getPlayerCount();
                int readyCount = roomMapper.getRoomPlayers(roomId).stream()
                        .mapToInt(p -> p.getStatus() == 1 ? 1 : 0)
                        .sum();

                // 如果所有玩家都准备好了，开始游戏
                if (playerCount > 0 && readyCount == playerCount) {
                    startGame(roomId);
                }

                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean leaveRoom(String roomId, String openId) {
        try {
            // 1. 获取房间信息
            Room room = roomMapper.findByRoomId(roomId);
            if (room == null) {
                return false;
            }

            // 2. 离开房间
            // 这里应该从房间玩家表中删除记录
            room.setPlayerCount(room.getPlayerCount() - 1);

            // 3. 如果房间为空，删除房间
            if (room.getPlayerCount() == 0) {
                roomCache.remove(roomId);
                roomMapper.deleteByRoomId(roomId);
            } else {
                // 更新缓存
                roomCache.put(roomId, room);
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @Transactional
    public boolean disbandRoom(String roomId, String openId) {
        try {
            // 1. 获取房间信息
            Room room = roomMapper.findByRoomId(roomId);
            if (room == null || !Objects.equals(room.getOwnerId(), openId)) {
                return false;
            }

            // 2. 删除房间
            roomCache.remove(roomId);
            roomMapper.deleteByRoomId(roomId);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成房间ID
     */
    private String generateRoomId() {
        return "ROOM_" + System.currentTimeMillis() + "_" + new Random().nextInt(1000);
    }

    /**
     * 获取默认规则
     */
    private Map<String, Object> getDefaultRules() {
        Map<String, Object> rules = new HashMap<>();
        // 基础规则
        rules.put("card_count", 54);
        rules.put("cards_per_player", 17);
        rules.put("first_hand_rule", "red_heart_4");
        rules.put("play_order", "clockwise");
        rules.put("play_time_limit", 30);

        // 出牌类型
        rules.put("enable_single", true);
        rules.put("enable_pair", true);
        rules.put("enable_three", true);
        rules.put("enable_three_with_two", true);
        rules.put("enable_straight", true);
        rules.put("enable_consecutive_pair", true);
        rules.put("enable_airplane", true);
        rules.put("enable_bomb", true);
        rules.put("enable_royal_bomb", true);

        return rules;
    }

    /**
     * 开始游戏
     */
    private void startGame(String roomId) {
        Room room = roomMapper.findByRoomId(roomId);
        if (room != null) {
            room.setStatus(1); // 游戏中
            room.setStartTime(new Date());
            roomMapper.updateStatus(roomId, 1);
            roomCache.put(roomId, room);
        }
    }

    /**
     * 转换为RoomInfoDTO
     */
    private RoomInfoDTO convertToRoomInfoDTO(Room room) {
        RoomInfoDTO roomInfo = new RoomInfoDTO();
        BeanUtils.copyProperties(room, roomInfo);

        // 解析规则快照
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> rules = objectMapper.readValue(room.getRulesSnapshot(), Map.class);
            roomInfo.setRules(rules);
        } catch (Exception e) {
            // 解析失败使用默认规则
            roomInfo.setRules(getDefaultRules());
        }

        // 获取玩家列表
        List<Player> players = roomMapper.getRoomPlayers(room.getRoomId());
        List<PlayerInfoDTO> playerInfos = new ArrayList<>();
        for (Player player : players) {
            PlayerInfoDTO playerInfo = new PlayerInfoDTO();
            BeanUtils.copyProperties(player, playerInfo);
            playerInfos.add(playerInfo);
        }
        roomInfo.setPlayers(playerInfos);

        return roomInfo;
    }
}