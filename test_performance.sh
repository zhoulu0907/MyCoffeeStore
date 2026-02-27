#!/bin/bash

# API 性能测试脚本
# 测试目标: 验证后端 API 响应时间符合预期

BASE_URL="http://localhost:8080/api"
TOKEN=""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 获取响应时间的函数
test_api() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    local header=$5
    local expected=$6

    # 构建curl命令
    if [ -z "$data" ]; then
        curl_cmd="curl -s -X $method '$url'"
    else
        curl_cmd="curl -s -X $method '$url' -H 'Content-Type: application/json'"
        if [ ! -z "$header" ]; then
            curl_cmd="$curl_cmd -H '$header'"
        fi
        curl_cmd="$curl_cmd -d '$data'"
    fi

    # 执行并测量时间
    start=$(python3 -c "import time; print(int(time.time()*1000))")
    response=$(eval $curl_cmd)
    end=$(python3 -c "import time; print(int(time.time()*1000))")
    time_taken=$((end - start))

    # 检查HTTP状态码
    http_code=$(curl -s -o /dev/null -w "%{http_code}" -X $method "$url" ${data:+-H "Content-Type: application/json" -d "$data"} ${header:+-H "$header"})

    # 判断状态
    status=""
    if echo "$response" | grep -q '"code":200'; then
        if [ $time_taken -le $expected ]; then
            status="${GREEN}✅ PASS${NC}"
        else
            status="${YELLOW}⚠️ SLOW${NC}"
        fi
    else
        status="${RED}❌ FAIL${NC}"
    fi

    echo "$name|$method|$url|$time_taken|$http_code|$status"
}

echo "========================================="
echo "    API 性能测试"
echo "========================================="
echo ""
echo "测试环境:"
echo "  - 后端: $BASE_URL"
echo "  - 测试时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "  - Commit: f411024 (feat: 添加历史订单数据生成器和用户中心功能)"
echo ""
echo "========================================="
echo "开始测试..."
echo "========================================="
echo ""

# 测试结果存储
results=()

# 1. 登录测试
echo "测试 1/14: 用户登录..."
result=$(test_api "用户登录" "POST" "$BASE_URL/v1/auth/login" '{"account":"zhoulu","password":"123456"}' "" "500")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s %s\n  方法: %s\n  响应时间: %dms\n  HTTP状态: %s\n  状态: %s\n", $1, $2, $3, $4, $5, $6}'
echo ""

# 提取token
TOKEN=$(curl -s -X POST "$BASE_URL/v1/auth/login" -H "Content-Type: application/json" -d '{"account":"zhoulu","password":"123456"}' | python3 -c "import sys, json; print(json.load(sys.stdin)['data']['token'])" 2>/dev/null)
if [ -z "$TOKEN" ]; then
    echo "警告: 无法获取token，部分测试将跳过"
    TOKEN="test-token"
fi
echo "Token: ${TOKEN:0:20}..."
echo ""

# 2. 咖啡列表
echo "测试 2/14: 咖啡列表..."
result=$(test_api "咖啡列表" "GET" "$BASE_URL/v1/coffee/list" "" "" "300")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 3. 咖啡详情
echo "测试 3/14: 咖啡详情..."
result=$(test_api "咖啡详情" "GET" "$BASE_URL/v1/coffee/detail?id=1" "" "" "100")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 4. 咖啡分类
echo "测试 4/14: 咖啡分类..."
result=$(test_api "咖啡分类" "GET" "$BASE_URL/v1/coffee/categories" "" "" "100")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 5. 购物车列表
echo "测试 5/14: 购物车列表..."
result=$(test_api "购物车列表" "GET" "$BASE_URL/v1/cart/list" "" "Authorization: Bearer $TOKEN" "300")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 6. 添加购物车
echo "测试 6/14: 添加购物车..."
result=$(test_api "添加购物车" "POST" "$BASE_URL/v1/cart/add" '{"coffeeId":1,"quantity":2}' "Authorization: Bearer $TOKEN" "500")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 7. 更新购物车
echo "测试 7/14: 更新购物车..."
result=$(test_api "更新购物车" "POST" "$BASE_URL/v1/cart/update" '{"coffeeId":1,"quantity":3}' "Authorization: Bearer $TOKEN" "500")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 8. 订单列表
echo "测试 8/14: 订单列表..."
result=$(test_api "订单列表" "GET" "$BASE_URL/v1/order/list" "" "Authorization: Bearer $TOKEN" "300")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 9. 用户余额
echo "测试 9/14: 用户余额..."
result=$(test_api "用户余额" "GET" "$BASE_URL/v1/user/balance" "" "Authorization: Bearer $TOKEN" "100")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 10. 创建订单
echo "测试 10/14: 创建订单..."
result=$(test_api "创建订单" "POST" "$BASE_URL/v1/order/create" '{"items":[{"coffeeId":1,"quantity":2}]}' "Authorization: Bearer $TOKEN" "500")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 11. 删除购物车商品
echo "测试 11/14: 删除购物车商品..."
result=$(test_api "删除购物车商品" "POST" "$BASE_URL/v1/cart/remove" '{"coffeeId":1}' "Authorization: Bearer $TOKEN" "500")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 12. 咖啡推荐
echo "测试 12/14: 咖啡推荐..."
result=$(test_api "咖啡推荐" "POST" "$BASE_URL/v1/recommendation" '{"preferences":["strong","sweet"],"priceRange":{"min":0,"max":10}}' "Authorization: Bearer $TOKEN" "1000")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 13. 用户充值
echo "测试 13/14: 用户充值..."
result=$(test_api "用户充值" "POST" "$BASE_URL/v1/user/recharge" '{"amount":100}' "Authorization: Bearer $TOKEN" "500")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

# 14. 登出
echo "测试 14/14: 用户登出..."
result=$(test_api "用户登出" "POST" "$BASE_URL/v1/auth/logout" "" "Authorization: Bearer $TOKEN" "500")
results+=("$result")
echo "$result" | awk -F'|' '{printf "  %-20s\n  响应时间: %dms\n  状态: %s\n", $1, $4, $6}'
echo ""

echo "========================================="
echo "    测试结果汇总"
echo "========================================="
echo ""
printf "%-25s %-8s %-15s %-10s %-10s\n" "端点" "方法" "响应时间" "HTTP状态" "测试状态"
echo "--------------------------------------------------------------------------------------------------------------"

for result in "${results[@]}"; do
    IFS='|' read -r name method url time http_code status <<< "$result"
    # 提取端点路径
    endpoint=$(echo $url | sed 's|/api/v1/||' | sed 's|?*||')
    printf "%-25s %-8s %-15s %-10s " "$endpoint" "$method" "${time}ms" "$http_code"
    echo -e "$status"
done

echo ""
echo "========================================="
echo "    性能分析"
echo "========================================="
