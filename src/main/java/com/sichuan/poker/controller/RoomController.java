package com.sichuan.poker.controller;

import com.sichuan.poker.dto.CreateRoomDTO;
import com.sichuan.poker.dto.RoomInfoDTO;
import com.sichuan.poker.entity.ApiResponse;
import com.sichuan.poker.entity.Room;
import com.sichuan.poker.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房间控制器
 */
@RestController
@RequestMapping("/api/room")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomService roomService;

    /**
     * 创建房间
     */
    @PostMapping("/create")
    public ApiResponse<RoomInfoDTO> createRoom(@RequestBody CreateRoomDTO createRoomDTO) {
        try {
            RoomInfoDTO roomInfo = roomService.createRoom(createRoomDTO);
            return ApiResponse.success(roomInfo);
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 获取房间列表
     */
    @GetMapping("/list")
    public ApiResponse<List<RoomInfoDTO>> getRoomList(
            @RequestParam(defaultValue = "2") Integer maxPlayers,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        try {
            List<RoomInfoDTO> roomList = roomService.getRoomList(maxPlayers, page, size);
            return ApiResponse.success(roomList);
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 获取房间详情
     */
    @GetMapping("/{roomId}")
    public ApiResponse<RoomInfoDTO> getRoomInfo(@PathVariable String roomId) {
        try {
            RoomInfoDTO roomInfo = roomService.getRoomInfo(roomId);
            if (roomInfo != null) {
                return ApiResponse.success(roomInfo);
            }
            return ApiResponse.fail(404, "房间不存在");
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 加入房间
     */
    @PostMapping("/{roomId}/join")
    public ApiResponse<Boolean> joinRoom(
            @PathVariable String roomId,
            @RequestParam String openId) {
        try {
            boolean result = roomService.joinRoom(roomId, openId);
            return result ? ApiResponse.success(true) : ApiResponse.fail(400, "加入房间失败");
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 玩家准备
     */
    @PostMapping("/{roomId}/ready")
    public ApiResponse<Boolean> playerReady(
            @PathVariable String roomId,
            @RequestParam String openId) {
        try {
            boolean result = roomService.playerReady(roomId, openId);
            return result ? ApiResponse.success(true) : ApiResponse.fail(400, "准备失败");
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 离开房间
     */
    @PostMapping("/{roomId}/leave")
    public ApiResponse<Boolean> leaveRoom(
            @PathVariable String roomId,
            @RequestParam String openId) {
        try {
            boolean result = roomService.leaveRoom(roomId, openId);
            return result ? ApiResponse.success(true) : ApiResponse.fail(400, "离开房间失败");
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 解散房间（房主专用）
     */
    @DeleteMapping("/{roomId}")
    public ApiResponse<Boolean> disbandRoom(
            @PathVariable String roomId,
            @RequestParam String openId) {
        try {
            boolean result = roomService.disbandRoom(roomId, openId);
            return result ? ApiResponse.success(true) : ApiResponse.fail(400, "解散房间失败");
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }
}