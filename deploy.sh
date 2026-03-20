#!/bin/bash

# 四川3带2扑克游戏后端部署脚本

set -e

echo "=========================================="
echo "四川3带2扑克游戏后端部署脚本"
echo "=========================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    log_info "Docker环境检查通过"
}

# 检查系统资源
check_resources() {
    log_info "检查系统资源..."

    # 检查内存
    total_mem=$(free -m | awk 'NR==2{printf "%.1f", $2/1024}')
    if (( $(echo "$total_mem < 2" | bc -l) )); then
        log_warn "系统内存不足2GB，建议至少4GB内存"
    else
        log_info "系统内存: ${total_mem}GB"
    fi

    # 检查CPU核心数
    cpu_cores=$(nproc)
    log_info "CPU核心数: ${cpu_cores}"
}

# 备份数据
backup_data() {
    if [ -d "mysql_data" ]; then
        log_info "备份数据库..."
        timestamp=$(date +%Y%m%d_%H%M%S)
        mkdir -p backups
        cp -r mysql_data backups/mysql_backup_${timestamp}
        log_info "数据库备份完成: backups/mysql_backup_${timestamp}"
    fi
}

# 构建和启动服务
deploy_services() {
    log_info "构建和启动服务..."

    # 构建后端服务
    log_info "构建后端服务..."
    docker-compose build --no-cache backend

    # 启动服务
    log_info "启动服务..."
    docker-compose up -d

    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30

    # 检查服务状态
    check_service_status
}

# 检查服务状态
check_service_status() {
    log_info "检查服务状态..."

    # 检查MySQL
    if docker-compose ps mysql | grep -q "Up"; then
        log_info "MySQL: 运行正常"
    else
        log_error "MySQL: 运行异常"
        return 1
    fi

    # 检查Redis
    if docker-compose ps redis | grep -q "Up"; then
        log_info "Redis: 运行正常"
    else
        log_error "Redis: 运行异常"
        return 1
    fi

    # 检查后端服务
    if docker-compose ps backend | grep -q "Up"; then
        log_info "后端服务: 运行正常"
    else
        log_error "后端服务: 运行异常"
        return 1
    fi

    # 检查后端健康状态
    health_status=$(curl -s http://localhost:8080/actuator/health || echo "down")
    if [ "$health_status" != "down" ]; then
        log_info "后端健康检查: 正常"
    else
        log_error "后端健康检查: 异常"
        return 1
    fi
}

# 初始化数据库
init_database() {
    log_info "初始化数据库..."

    # 等待MySQL启动完成
    log_info "等待MySQL启动..."
    while ! docker-compose exec mysql mysqladmin ping -hlocalhost --silent; do
        sleep 2
    done

    # 导入数据库初始化脚本
    if [ -f "src/main/resources/schema.sql" ]; then
        log_info "导入数据库结构..."
        docker-compose exec -T mysql mysql -uroot -p123456 sichuan_poker < src/main/resources/schema.sql
        log_info "数据库初始化完成"
    else
        log_warn "数据库初始化脚本未找到"
    fi
}

# 显示访问信息
show_access_info() {
    log_info "=========================================="
    log_info "部署完成！访问信息："
    log_info "后端API: http://localhost:8080"
    log_info "API文档: http://localhost:8080/swagger-ui.html"
    log_info "健康检查: http://localhost:8080/actuator/health"
    log_info "数据库: localhost:3306"
    log_info "Redis: localhost:6379"
    log_info "=========================================="
    log_info "常用命令："
    log_info "  查看日志: docker-compose logs -f [service]"
    log_info "  停止服务: docker-compose down"
    log_info "  重启服务: docker-compose restart"
    log_info "  更新部署: docker-compose up -d --build"
}

# 主函数
main() {
    log_info "开始部署四川3带2扑克游戏后端..."

    # 检查环境
    check_docker
    check_resources

    # 备份数据
    backup_data

    # 构建和部署
    deploy_services

    # 初始化数据库
    init_database

    # 显示访问信息
    show_access_info

    log_info "部署完成！"
}

# 捕获中断信号
trap 'log_error "部署被中断"; exit 1' INT

# 执行主函数
main "$@"