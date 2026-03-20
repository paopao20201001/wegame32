package com.sichuan.poker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务
 */
@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存玩家信息
     */
    @Cacheable(value = "player", key = "#openId")
    public Object getPlayerCache(String openId) {
        return redisTemplate.opsForValue().get("player:" + openId);
    }

    /**
     * 设置玩家信息缓存
     */
    @CacheEvict(value = "player", key = "#openId")
    public void setPlayerCache(String openId, Object player, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set("player:" + openId, player, timeout, timeUnit);
    }

    /**
     * 删除玩家信息缓存
     */
    @CacheEvict(value = "player", key = "#openId")
    public void evictPlayerCache(String openId) {
        redisTemplate.delete("player:" + openId);
    }

    /**
     * 缓存房间信息
     */
    @Cacheable(value = "room", key = "#roomId")
    public Object getRoomCache(String roomId) {
        return redisTemplate.opsForValue().get("room:" + roomId);
    }

    /**
     * 设置房间信息缓存
     */
    @CacheEvict(value = "room", key = "#roomId")
    public void setRoomCache(String roomId, Object room, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set("room:" + roomId, room, timeout, timeUnit);
    }

    /**
     * 删除房间信息缓存
     */
    @CacheEvict(value = "room", key = "#roomId")
    public void evictRoomCache(String roomId) {
        redisTemplate.delete("room:" + roomId);
    }

    /**
     * 缓存游戏状态
     */
    @Cacheable(value = "gameState", key = "#roomId")
    public Object getGameStateCache(String roomId) {
        return redisTemplate.opsForValue().get("gameState:" + roomId);
    }

    /**
     * 设置游戏状态缓存
     */
    @CacheEvict(value = "gameState", key = "#roomId")
    public void setGameStateCache(String roomId, Object gameState, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set("gameState:" + roomId, gameState, timeout, timeUnit);
    }

    /**
     * 删除游戏状态缓存
     */
    @CacheEvict(value = "gameState", key = "#roomId")
    public void evictGameStateCache(String roomId) {
        redisTemplate.delete("gameState:" + roomId);
    }

    /**
     * 缓存规则
     */
    @Cacheable(value = "rules", key = "#ruleKey")
    public Object getRulesCache(String ruleKey) {
        return redisTemplate.opsForValue().get("rules:" + ruleKey);
    }

    /**
     * 设置规则缓存
     */
    @CacheEvict(value = "rules", key = "#ruleKey")
    public void setRulesCache(String ruleKey, Object rules, long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set("rules:" + ruleKey, rules, timeout, timeUnit);
    }

    /**
     * 删除规则缓存
     */
    @CacheEvict(value = "rules", key = "#ruleKey")
    public void evictRulesCache(String ruleKey) {
        redisTemplate.delete("rules:" + ruleKey);
    }

    /**
     * 设置房间玩家会话
     */
    public void setRoomPlayerSession(String roomId, String playerId, Object session) {
        String key = "roomSession:" + roomId + ":" + playerId;
        redisTemplate.opsForValue().set(key, session, 2, TimeUnit.HOURS); // 2小时过期
    }

    /**
     * 获取房间玩家会话
     */
    public Object getRoomPlayerSession(String roomId, String playerId) {
        String key = "roomSession:" + roomId + ":" + playerId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除房间玩家会话
     */
    public void evictRoomPlayerSession(String roomId, String playerId) {
        String key = "roomSession:" + roomId + ":" + playerId;
        redisTemplate.delete(key);
    }

    /**
     * 设置房间玩家在线状态
     */
    public void setPlayerOnline(String roomId, String playerId, boolean online) {
        String key = "roomPlayerOnline:" + roomId + ":" + playerId;
        redisTemplate.opsForValue().set(key, online, 1, TimeUnit.HOURS); // 1小时过期
    }

    /**
     * 获取房间玩家在线状态
     */
    public boolean getPlayerOnline(String roomId, String playerId) {
        String key = "roomPlayerOnline:" + roomId + ":" + playerId;
        Object online = redisTemplate.opsForValue().get(key);
        return online != null && (Boolean) online;
    }

    /**
     * 增加房间玩家计数
     */
    public void incrementRoomPlayerCount(String roomId) {
        String key = "roomPlayerCount:" + roomId;
        redisTemplate.opsForValue().increment(key);
        // 设置1小时过期
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    /**
     * 减少房间玩家计数
     */
    public void decrementRoomPlayerCount(String roomId) {
        String key = "roomPlayerCount:" + roomId;
        redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 获取房间玩家数量
     */
    public Long getRoomPlayerCount(String roomId) {
        String key = "roomPlayerCount:" + roomId;
        Object count = redisTemplate.opsForValue().get(key);
        return count != null ? (Long) count : 0L;
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
}