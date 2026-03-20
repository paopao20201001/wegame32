#!/bin/bash

# 四川3带2扑克游戏后端代码推送脚本
# 使用说明：
# 1. 确保已经添加了GitHub Personal Access Token
# 2. 运行脚本并按照提示输入GitHub用户名和token

echo "=========================================="
echo "四川3带2扑克游戏后端代码推送脚本"
echo "=========================================="

# 检查当前状态
echo "检查Git状态..."
git status

# 添加所有文件
echo "添加所有文件到Git..."
git add .

# 提交更改
echo "提交更改..."
git commit -m "feat: Implement Sichuan 3-2 Poker Game Backend

- Add WeChat authorization login
- Implement room management system
- Create dynamic rule configuration
- Add game play validation engine
- Implement player scoring system
- Add database schema and MyBatis mappers
- Support RESTful API endpoints

Features:
- Player authentication with WeChat
- Room creation and management
- Game rules configuration
- Card play validation
- Game state management
- Score calculation"

# 推送到GitHub
echo "正在推送到GitHub..."
echo "请在弹出的对话框中输入您的GitHub用户名和Personal Access Token"

git push origin master

echo "推送完成！"
echo "=========================================="