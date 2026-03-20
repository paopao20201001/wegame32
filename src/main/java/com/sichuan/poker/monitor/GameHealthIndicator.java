package com.sichuan.poker.monitor;

import com.sichuan.poker.service.CacheService;
import com.sichuan.poker.repository.RoomMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * 自定义健康检查
 */
@Component
public class GameHealthIndicator implements HealthIndicator {

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private CacheService cacheService;

    @Override
    public Health health() {
        try {
            // 检查数据库连接
            int roomCount = roomMapper.findWaitingRooms(4).size();
            if (roomCount >= 0) {
                // 检查Redis连接
                String testKey = "health:test:" + System.currentTimeMillis();
                cacheService.setRoomCache(testKey, "test", 60, java.util.concurrent.TimeUnit.SECONDS);
                String value = (String) cacheService.getRoomCache(testKey);

                if ("test".equals(value)) {
                    // 检查通过
                    return Health.up()
                            .withDetail("database", "Connected")
                            .withDetail("redis", "Connected")
                            .withDetail("roomCount", roomCount)
                            .build();
                }
            }

            // 检查失败
            return Health.down()
                    .withDetail("error", "Health check failed")
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}