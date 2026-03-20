# 四川3带2扑克游戏 - 前后端集成接口文档

## 1. 基础配置

### API基础URL
- 开发环境: `http://localhost:8080`
- 生产环境: `https://your-domain.com`

### WebSocket连接
- 连接地址: `ws://localhost:8080/ws/game?playerId={playerId}`

## 2. WebSocket实时通信

### 连接建立
```javascript
const ws = new WebSocket(`ws://localhost:8080/ws/game?playerId=${playerId}`);
```

### 事件监听
```javascript
ws.onmessage = function(event) {
    const message = JSON.parse(event.data);
    handleGameMessage(message);
};

ws.onclose = function(event) {
    console.log('WebSocket连接关闭');
    handleDisconnect();
};

ws.onerror = function(error) {
    console.error('WebSocket错误:', error);
};
```

### 消息格式

#### 加入房间
```json
{
  "type": "join_room",
  "roomId": "ROOM_123456",
  "timestamp": 1634567890000
}
```

#### 离开房间
```json
{
  "type": "leave_room",
  "roomId": "ROOM_123456",
  "timestamp": 1634567890000
}
```

#### 游戏动作（出牌/过牌）
```json
{
  "type": "game_action",
  "roomId": "ROOM_123456",
  "actionType": 1, // 1-出牌, 2-过牌
  "cardIds": ["H3", "H4", "H5"],
  "timestamp": 1634567890000
}
```

#### 心跳检测
```json
{
  "type": "heartbeat",
  "timestamp": 1634567890000
}
```

### 服务器消息类型

#### 玩家加入房间
```json
{
  "type": "player_joined",
  "success": true,
  "data": {
    "roomId": "ROOM_123456",
    "players": ["player1", "player2"]
  },
  "timestamp": 1634567890000
}
```

#### 玩家离开房间
```json
{
  "type": "player_left",
  "success": true,
  "data": {
    "playerId": "player1"
  },
  "timestamp": 1634567890000
}
```

#### 游戏动作广播
```json
{
  "type": "game_action",
  "success": true,
  "data": {
    "playerId": "player1",
    "actionType": 1,
    "cardIds": ["H3", "H4", "H5"]
  },
  "timestamp": 1634567890000
}
```

#### 游戏状态更新
```json
{
  "type": "game_state",
  "success": true,
  "data": {
    "roomId": "ROOM_123456",
    "state": "waiting",
    "currentTime": 1634567890000
  },
  "timestamp": 1634567890000
}
```

## 3. RESTful API接口

### 3.1 玩家相关接口

#### 微信登录
```javascript
// 请求
fetch('/api/player/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    code: '微信授权码',
    nickName: '玩家昵称',
    avatarUrl: '头像URL'
  })
});

// 响应
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "openId": "openid123",
    "nickName": "玩家昵称",
    "avatarUrl": "头像URL",
    "level": 1,
    "score": 1000,
    "winCount": 0,
    "loseCount": 0,
    "winRate": 0.0
  }
}
```

#### 获取玩家信息
```javascript
fetch(`/api/player/info/${openId}`)
  .then(response => response.json())
  .then(data => console.log(data));
```

#### 实名认证
```javascript
fetch(`/api/player/realname/${openId}?realName=姓名&idCard=身份证号`, {
  method: 'POST'
})
  .then(response => response.json());
```

### 3.2 房间相关接口

#### 创建房间
```javascript
fetch('/api/room/create', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    roomName: '房间名称',
    maxPlayers: 4,
    isPrivate: false,
    password: null,
    rules: {
      card_count: 54,
      cards_per_player: 17
    }
  })
});
```

#### 获取房间列表
```javascript
fetch('/api/room/list?maxPlayers=4&page=0&size=10')
  .then(response => response.json())
  .then(data => console.log(data));
```

#### 获取房间详情
```javascript
fetch(`/api/room/${roomId}`)
  .then(response => response.json())
  .then(data => console.log(data));
```

#### 加入房间
```javascript
fetch(`/api/room/${roomId}/join?openId=${openId}`, {
  method: 'POST'
});
```

#### 玩家准备
```javascript
fetch(`/api/room/${roomId}/ready?openId=${openId}`, {
  method: 'POST'
});
```

### 3.3 游戏相关接口

#### 出牌
```javascript
fetch('/api/game/play', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    roomId: "ROOM_123456",
    playerId: 1,
    cardIds: ["H3", "H4", "H5"],
    actionType: 1,
    lastPlayId: 1
  })
});
```

#### 过牌
```javascript
fetch(`/api/game/pass?roomId=${roomId}&openId=${openId}`, {
  method: 'POST'
});
```

#### 获取游戏状态
```javascript
fetch(`/api/game/state/${roomId}`)
  .then(response => response.json())
  .then(data => console.log(data));
```

### 3.4 WebSocket相关接口

#### 发送消息到房间
```javascript
fetch(`/api/ws/room/${roomId}/send`, {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    message: "游戏消息",
    type: "notification"
  })
});
```

#### 获取房间在线玩家
```javascript
fetch(`/api/ws/room/${roomId}/players`)
  .then(response => response.json())
  .then(data => console.log(data));
```

## 4. 错误处理

### HTTP状态码
- 200: 请求成功
- 400: 请求参数错误
- 401: 未授权
- 404: 资源不存在
- 500: 服务器内部错误

### 错误响应格式
```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

### 前端错误处理示例
```javascript
async function handleLogin(code) {
  try {
    const response = await fetch('/api/player/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ code, nickName, avatarUrl })
    });

    const result = await response.json();

    if (result.code === 200) {
      // 登录成功
      console.log('登录成功:', result.data);
    } else {
      // 登录失败
      console.error('登录失败:', result.message);
      showToast(result.message);
    }
  } catch (error) {
    console.error('网络错误:', error);
    showToast('网络连接失败');
  }
}
```

## 5. 断线重连机制

### 重连策略
1. 检测到连接断开时，立即显示断线提示
2. 30秒后自动重连
3. 重连成功后恢复游戏状态
4. 重连失败继续等待并再次尝试

### 前端重连实现
```javascript
class GameWebSocket {
  constructor(url, playerId) {
    this.url = url;
    this.playerId = playerId;
    this.ws = null;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.reconnectInterval = 30000; // 30秒
  }

  connect() {
    this.ws = new WebSocket(`${this.url}?playerId=${this.playerId}`);

    this.ws.onopen = () => {
      console.log('WebSocket连接成功');
      this.reconnectAttempts = 0;
    };

    this.ws.onclose = (event) => {
      console.log('WebSocket连接关闭');
      this.handleDisconnect();
    };

    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error);
    };
  }

  handleDisconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      setTimeout(() => {
        this.reconnectAttempts++;
        console.log(`尝试第${this.reconnectAttempts}次重连...`);
        this.connect();
      }, this.reconnectInterval);
    } else {
      console.log('重连次数超限，请手动刷新页面');
    }
  }
}
```

## 6. 心跳检测

### 客户端心跳
```javascript
setInterval(() => {
  if (this.ws && this.ws.readyState === WebSocket.OPEN) {
    this.ws.send(JSON.stringify({
      type: 'heartbeat',
      timestamp: Date.now()
    }));
  }
}, 30000); // 30秒一次
```

### 服务器心跳响应
```json
{
  "type": "heartbeat",
  "success": true,
  "data": {
    "timestamp": 1634567890000,
    "playerId": "player1"
  },
  "timestamp": 1634567890000
}
```

## 7. 实时游戏状态同步

### 游戏状态定义
- `waiting`: 等待中
- `playing`: 游戏进行中
- `ended`: 游戏结束

### 状态同步流程
1. 玩家加入房间 → 广播 `player_joined`
2. 玩家准备 → 广播 `player_ready`
3. 游戏开始 → 广播 `game_started`
4. 玩家出牌 → 广播 `game_action`
5. 玩家过牌 → 广播 `game_pass`
6. 游戏结束 → 广播 `game_ended`

## 8. 部署和配置

### 环境变量
```bash
# 数据库配置
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=sichuan_poker
MYSQL_USERNAME=poker_user
MYSQL_PASSWORD=poker_password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# 微信配置
WX_APPID=your_appid
WX_SECRET=your_secret

# 文件上传配置
UPLOAD_PATH=/app/uploads
```

### Docker Compose启动
```bash
docker-compose up -d
```

### 健康检查
```bash
curl http://localhost:8080/actuator/health
```

## 9. 安全注意事项

1. 所有API接口都需要进行身份验证
2. WebSocket连接需要进行权限验证
3. 敏感数据如密码等需要加密传输
4. 防止SQL注入和XSS攻击
5. 限制请求频率防止DDoS攻击