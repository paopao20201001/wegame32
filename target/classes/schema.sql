-- 四川3带2扑克游戏数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS sichuan_poker DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE sichuan_poker;

-- 玩家表
CREATE TABLE IF NOT EXISTS player (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    open_id VARCHAR(64) NOT NULL COMMENT '微信OpenID',
    union_id VARCHAR(64) COMMENT '微信UnionID',
    nick_name VARCHAR(128) COMMENT '昵称',
    avatar_url VARCHAR(512) COMMENT '头像URL',
    level INT DEFAULT 1 COMMENT '等级',
    score INT DEFAULT 1000 COMMENT '积分',
    win_count INT DEFAULT 0 COMMENT '胜利次数',
    lose_count INT DEFAULT 0 COMMENT '失败次数',
    last_login_time DATETIME COMMENT '最后登录时间',
    status INT DEFAULT 0 COMMENT '状态：0-离线，1-在线，2-游戏中',
    is_real_name_auth TINYINT DEFAULT 0 COMMENT '是否实名认证',
    real_name VARCHAR(32) COMMENT '真实姓名',
    id_card VARCHAR(18) COMMENT '身份证号',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_open_id (open_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家表';

-- 游戏规则表
CREATE TABLE IF NOT EXISTS game_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_key VARCHAR(64) NOT NULL COMMENT '规则键',
    rule_name VARCHAR(128) NOT NULL COMMENT '规则名称',
    rule_value TEXT COMMENT '规则值',
    rule_type TINYINT NOT NULL COMMENT '规则类型：1-全服配置，2-房间配置',
    room_id BIGINT COMMENT '房间ID（房间配置时使用）',
    is_default TINYINT DEFAULT 0 COMMENT '是否是默认配置',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    description VARCHAR(512) COMMENT '规则描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_rule_key (rule_key),
    KEY idx_room_id (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏规则表';

-- 房间表
CREATE TABLE IF NOT EXISTS room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id VARCHAR(32) NOT NULL COMMENT '房间唯一标识',
    room_name VARCHAR(128) COMMENT '房间名称',
    owner_id BIGINT NOT NULL COMMENT '房主ID',
    player_count INT DEFAULT 0 COMMENT '玩家数量',
    max_players INT NOT NULL COMMENT '最大玩家数',
    status TINYINT DEFAULT 0 COMMENT '状态：0-等待中，1-游戏中，2-已结束',
    `password` VARCHAR(32) COMMENT '房间密码',
    is_private TINYINT DEFAULT 0 COMMENT '是否私有房间',
    rules_snapshot TEXT COMMENT '规则快照（JSON格式）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    UNIQUE KEY uk_room_id (room_id),
    KEY idx_owner_id (owner_id),
    KEY idx_status (status),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间表';

-- 房间玩家关联表
CREATE TABLE IF NOT EXISTS room_player (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_id BIGINT NOT NULL COMMENT '房间ID',
    player_id BIGINT NOT NULL COMMENT '玩家ID',
    seat_index INT COMMENT '座位索引',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未准备，1-已准备，2-游戏中',
    is_owner TINYINT DEFAULT 0 COMMENT '是否房主',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ready_time DATETIME COMMENT '准备时间',
    UNIQUE KEY uk_room_player (room_id, player_id),
    KEY idx_room_id (room_id),
    KEY idx_player_id (player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='房间玩家关联表';

-- 游戏记录表
CREATE TABLE IF NOT EXISTS game_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_id VARCHAR(32) NOT NULL COMMENT '游戏ID',
    room_id BIGINT NOT NULL COMMENT '房间ID',
    start_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME COMMENT '结束时间',
    duration INT COMMENT '游戏时长（秒）',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未开始，1-进行中，2-已结束',
    winner_id BIGINT COMMENT '获胜者ID',
    winner_name VARCHAR(128) COMMENT '获胜者名称',
    winner_score INT COMMENT '获胜者分数',
    settlement_info TEXT COMMENT '结算信息（JSON格式）',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_game_id (game_id),
    KEY idx_room_id (room_id),
    KEY idx_winner_id (winner_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏记录表';

-- 玩家游戏记录表
CREATE TABLE IF NOT EXISTS player_game_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    game_record_id BIGINT NOT NULL COMMENT '游戏记录ID',
    player_id BIGINT NOT NULL COMMENT '玩家ID',
    player_name VARCHAR(128) NOT NULL COMMENT '玩家名称',
    initial_score INT NOT NULL COMMENT '初始分数',
    final_score INT NOT NULL COMMENT '最终分数',
    score_change INT COMMENT '分数变化',
    card_count INT DEFAULT 0 COMMENT '剩余牌数',
    `rank` INT COMMENT '排名',
    is_winner TINYINT DEFAULT 0 COMMENT '是否获胜',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_game_record_id (game_record_id),
    KEY idx_player_id (player_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='玩家游戏记录表';

-- 插入默认游戏规则
INSERT INTO game_rule (rule_key, rule_name, rule_value, rule_type, is_default, description) VALUES
('card_count', '牌数', '54', 1, 1, '游戏使用牌数：52/54'),
('cards_per_player', '每人牌数', '17', 1, 1, '每人发牌数量'),
('first_hand_rule', '先手规则', 'red_heart_4', 1, 1, '先手规则：red_heart_4/random/landlord'),
('play_order', '出牌顺序', 'clockwise', 1, 1, '出牌顺序：clockwise/counterclockwise'),
('play_time_limit', '出牌时间限制', '30', 1, 1, '出牌时间限制（秒）'),
('enable_single', '单张', 'true', 1, 1, '是否启用单张'),
('enable_pair', '对子', 'true', 1, 1, '是否启用对子'),
('enable_three', '三张', 'true', 1, 1, '是否启用三张'),
('enable_three_with_two', '3带2', 'true', 1, 1, '是否启用3带2'),
('enable_straight', '顺子', 'true', 1, 1, '是否启用顺子'),
('enable_consecutive_pair', '连对', 'true', 1, 1, '是否启用连对'),
('enable_airplane', '飞机', 'true', 1, 1, '是否启用飞机'),
('enable_bomb', '炸弹', 'true', 1, 1, '是否启用炸弹'),
('enable_royal_bomb', '王炸', 'true', 1, 1, '是否启用王炸'),
('force_4444', '4444强制出牌', 'false', 1, 1, '手中有4个4时必须出'),
('force_bomb_response', '炸弹强制响应', 'false', 1, 1, '上家出炸弹，必须出炸弹'),
('first_must_red_heart_4', '首出必含红桃4', 'false', 1, 1, '首轮出牌必须包含红桃4'),
('bomb_suppress_range', '炸弹压制范围', 'all', 1, 1, '炸弹压制范围：all/same_type'),
('base_score', '基础分', '10', 1, 1, '获胜基础分数'),
('bomb_multiplier', '炸弹倍数', '2', 1, 1, '炸弹倍数'),
('win_streak_bonus', '连胜加成', '0.1', 1, 1, '连胜奖励系数'),
('remaining_card_penalty', '剩余牌惩罚', '0.5', 1, 1, '剩余牌扣分系数');