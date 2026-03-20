package com.sichuan.poker.controller;

import com.sichuan.poker.dto.PlayCardDTO;
import com.sichuan.poker.entity.ApiResponse;
import com.sichuan.poker.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 游戏控制器
 */
@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {

    @Autowired
    private GameService gameService;

    /**
     * 出牌
     */
    @PostMapping("/play")
    public ApiResponse<Boolean> playCard(@RequestBody PlayCardDTO playCardDTO) {
        try {
            boolean result = gameService.playCard(playCardDTO);
            return result ? ApiResponse.success(true) : ApiResponse.fail(400, "出牌失败");
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 过牌
     */
    @PostMapping("/pass")
    public ApiResponse<Boolean> pass(@RequestParam String roomId, @RequestParam String openId) {
        try {
            boolean result = gameService.pass(roomId, openId);
            return result ? ApiResponse.success(true) : ApiResponse.fail(400, "过牌失败");
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 获取游戏状态
     */
    @GetMapping("/state/{roomId}")
    public ApiResponse<Object> getGameState(@PathVariable String roomId) {
        try {
            Object gameState = gameService.getGameState(roomId);
            return ApiResponse.success(gameState);
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }

    /**
     * 获取游戏记录
     */
    @GetMapping("/record/{gameId}")
    public ApiResponse<Object> getGameRecord(@PathVariable String gameId) {
        try {
            Object gameRecord = gameService.getGameRecord(gameId);
            return ApiResponse.success(gameRecord);
        } catch (Exception e) {
            return ApiResponse.fail(500, e.getMessage());
        }
    }
}