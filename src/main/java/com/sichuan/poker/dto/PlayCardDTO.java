package com.sichuan.poker.dto;

import lombok.Data;
import java.util.List;

/**
 * 出牌请求DTO
 */
@Data
public class PlayCardDTO {
    private String roomId;
    private Long playerId;
    private List<String> cardIds; // 出的牌ID列表
    private Integer actionType;   // 1-出牌，2-过牌
    private Long lastPlayId;      // 上一次出牌记录ID
}