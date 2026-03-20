# 四川3带2扑克游戏后端 Dockerfile
# 基于OpenJDK 8
FROM openjdk:8-jre-alpine

# 设置工作目录
WORKDIR /app

# 安装必要的工具
RUN apk add --no-cache curl

# 创建日志目录
RUN mkdir -p /app/logs

# 复制Maven配置文件
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
COPY mvnw.cmd .

# 下载依赖（利用Docker缓存）
RUN ./mvnw dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用
RUN ./mvnw package -DskipTests -B

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动命令
CMD ["java", "-Xmx512m", "-Xms256m", "-jar", "target/sichuan-poker-game-backend-1.0.jar"]
