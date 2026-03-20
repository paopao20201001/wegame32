# 四川3带2扑克游戏后端

基于Spring Boot 2.5.5的微信小游戏后端服务，支持四川3带2扑克游戏的核心功能。

## 功能特性

- 🔐 微信授权登录
- 🏠 房间管理（创建、加入、准备、解散）
- 🃏 游戏规则配置化
- 🎮 游戏出牌校验
- 📊 游戏记录和积分系统
- 🗄️ MySQL数据库支持

## 技术栈

- Spring Boot 2.5.5
- MyBatis
- MySQL 8.0
- Maven 3.6+

## 快速开始

### 1. 环境要求

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库初始化

```bash
# 导入数据库脚本
mysql -u root -p < src/main/resources/schema.sql
```

### 3. 配置数据库

修改 `application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sichuan_poker?useSSL=false&serverTimezone=UTC
    username: your_username
    password: your_password
```

### 4. 配置微信小程序

在 `application.yml` 中配置微信小程序的AppID和Secret：

```yaml
wx:
  appid: your_appid
  secret: your_secret
```

### 5. 启动应用

```bash
# 使用Maven
mvn spring-boot:run

# 或者直接运行
java -jar target/sichuan-poker-game-backend-1.0.jar
```

应用将在 http://localhost:8080 启动

## API文档

### 玩家相关

#### 微信登录
```
POST /api/player/login
Content-Type: application/json

{
  "code": "微信授权码",
  "nickName": "玩家昵称",
  "avatarUrl": "头像URL"
}
```

#### 获取玩家信息
```
GET /api/player/info/{openId}
```

#### 实名认证
```
POST /api/player/realname/{openId}?realName=姓名&idCard=身份证号
```

### 房间相关

#### 创建房间
```
POST /api/room/create
Content-Type: application/json

{
  "roomName": "房间名称",
  "maxPlayers": 4,
  "isPrivate": false,
  "password": null,
  "rules": {
    "card_count": 54,
    "cards_per_player": 17
  }
}
```

#### 获取房间列表
```
GET /api/room/list?maxPlayers=4&page=0&size=10
```

#### 加入房间
```
POST /api/room/{roomId}/join?openId=玩家OpenID
```

#### 玩家准备
```
POST /api/room/{roomId}/ready?openId=玩家OpenID
```

### 游戏相关

#### 出牌
```
POST /api/game/play
Content-Type: application/json

{
  "roomId": "房间ID",
  "playerId": 1,
  "cardIds": ["H3", "H4", "H5"],
  "actionType": 1,
  "lastPlayId": 1
}
```

#### 过牌
```
POST /api/game/pass?roomId=房间ID&openId=玩家OpenID
```

#### 获取游戏状态
```
GET /api/game/state/{roomId}
```

## 数据库表结构

### player - 玩家表
- id: 主键
- open_id: 微信OpenID
- union_id: 微信UnionID
- nick_name: 昵称
- avatar_url: 头像URL
- level: 等级
- score: 积分
- win_count: 胜利次数
- lose_count: 失败次数
- status: 状态

### room - 房间表
- id: 主键
- room_id: 房间唯一标识
- room_name: 房间名称
- owner_id: 房主ID
- player_count: 玩家数量
- max_players: 最大玩家数
- status: 状态
- rules_snapshot: 规则快照

### game_rule - 游戏规则表
- id: 主键
- rule_key: 规则键
- rule_value: 规则值
- rule_type: 规则类型

### game_record - 游戏记录表
- id: 主键
- game_id: 游戏ID
- room_id: 房间ID
- winner_id: 获胜者ID
- settlement_info: 结算信息

## 部署说明

### 使用Docker

```bash
# 构建镜像
docker build -t sichuan-poker-backend .

# 运行容器
docker run -d -p 8080:8080 \
  -e MYSQL_HOST=host.docker.internal \
  -e MYSQL_USERNAME=root \
  -e MYSQL_PASSWORD=123456 \
  sichuan-poker-backend
```

### 使用JAR包

```bash
# 打包
mvn clean package

# 运行
java -jar sichuan-poker-game-backend-1.0.jar
```

## 开发说明

### 项目结构

```
src/main/java/com/sichuan/poker/
├── controller/     # 控制器层
├── service/        # 业务逻辑层
├── repository/     # 数据访问层
├── entity/        # 实体类
├── dto/           # 数据传输对象
├── config/        # 配置类
└── utils/         # 工具类
```

### 核心功能

1. **规则引擎**：支持动态规则配置和验证
2. **房间管理**：创建、加入、准备、解散房间
3. **游戏逻辑**：出牌校验、游戏流程控制
4. **积分系统**：计算得分、更新玩家积分

## 许可证

MIT License
