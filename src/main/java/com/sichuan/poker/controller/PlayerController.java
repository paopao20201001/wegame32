package com.sichuan.poker.controller;

import com.sichuan.poker.dto.PlayerInfoDTO;
import com.sichuan.poker.dto.PlayerLoginDTO;
import com.sichuan.poker.entity.ApiResponse;
import com.sichuan.poker.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 玩家控制器
 */
@RestController
@RequestMapping("/api/player")
@CrossOrigin(origins = "*")
public class PlayerController {

    @Autowired
    private PlayerService playerService;

    /**
     * 微信登录
     */
    @PostMapping("/login")
    public ApiResponse<PlayerInfoDTO> login(@RequestBody PlayerLoginDTO loginDTO) {
        try {
            PlayerInfoDTO playerInfo = playerService.login(loginDTO);
            return ApiResponse.success(playerInfo);
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 获取玩家信息
     */
    @GetMapping("/info/{openId}")
    public ApiResponse<PlayerInfoDTO> getPlayerInfo(@PathVariable String openId) {
        try {
            PlayerInfoDTO playerInfo = playerService.getPlayerInfo(openId);
            if (playerInfo != null) {
                return ApiResponse.success(playerInfo);
            }
            return ApiResponse.fail(404, "玩家不存在");
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 更新玩家信息
     */
    @PutMapping("/info/{openId}")
    public ApiResponse<Boolean> updatePlayerInfo(@PathVariable String openId, @RequestBody PlayerInfoDTO playerInfo) {
        try {
            boolean result = playerService.updatePlayerInfo(openId, playerInfo);
            return result ? ApiResponse.success(true) : ApiResponse.fail(400, "更新失败");
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 实名认证
     */
    @PostMapping("/realname/{openId}")
    public ApiResponse<Boolean> realNameAuth(
            @PathVariable String openId,
            @RequestParam String realName,
            @RequestParam String idCard) {
        try {
            boolean result = playerService.realNameAuth(openId, realName, idCard);
            return result ? ApiResponse.success(true) : ApiResponse.fail(400, "实名认证失败");
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }
}