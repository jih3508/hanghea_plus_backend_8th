#!/bin/bash

# 실제 코드 기반 Redis 데이터 생성 스크립트

echo "🚀 실제 애플리케이션 기반 Redis 데이터 생성 시작..."

# Redis 연결 확인
if ! redis-cli ping > /dev/null 2>&1; then
    echo "❌ Redis 서버에 연결할 수 없습니다."
    exit 1
fi

echo "✅ Redis 연결 확인 완료"

# 날짜 계산
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    YESTERDAY=$(date -v-1d +%Y%m%d)
    TODAY=$(date +%Y%m%d)
    TOMORROW=$(date -v+1d +%Y%m%d)
    DAY_AFTER_TOMORROW=$(date -v+2d +%Y%m%d)
else
    # Linux
    YESTERDAY=$(date -d "yesterday" +%Y%m%d)
    TODAY=$(date +%Y%m%d)
    TOMORROW=$(date -d "tomorrow" +%Y%m%d)
    DAY_AFTER_TOMORROW=$(date -d "+2 days" +%Y%m%d)
fi

echo "📅 날짜 설정:"
echo "  어제: $YESTERDAY"
echo "  오늘: $TODAY"
echo "  내일: $TOMORROW"
echo "  모레: $DAY_AFTER_TOMORROW"

# 1. 인기 상품 데이터 생성
echo ""
echo "🏆 인기 상품 랭킹 데이터 생성..."

# 어제 데이터 (getTopProducts()에서 조회)
redis-cli ZADD product:rank:$YESTERDAY 150 1 120 2 100 3 95 4 80 5 75 6 60 7 50 8 40 9 30 10 > /dev/null

# 오늘 데이터  
redis-cli ZADD product:rank:$TODAY 85 1 70 2 65 3 55 4 45 5 40 6 35 7 25 8 20 9 15 10 > /dev/null

# 내일 데이터
redis-cli ZADD product:rank:$TOMORROW 25 1 20 2 15 3 10 4 8 5 > /dev/null

# 모레 데이터
redis-cli ZADD product:rank:$DAY_AFTER_TOMORROW 15 1 12 2 10 3 8 4 5 5 > /dev/null

echo "  ✅ 어제 랭킹: Top 10 상품 생성"
echo "  ✅ 오늘 랭킹: Top 10 상품 생성"
echo "  ✅ 내일 랭킹: Top 5 상품 생성"
echo "  ✅ 모레 랭킹: Top 5 상품 생성"

# 2. 쿠폰 재고 데이터 생성
echo ""
echo "🎫 쿠폰 재고 데이터 생성..."

# 일반 쿠폰들
redis-cli MSET \
  coupon:quantity:1 100 \
  coupon:quantity:2 50 \
  coupon:quantity:3 200 \
  coupon:quantity:4 75 \
  coupon:quantity:5 30 \
  coupon:quantity:6 150 \
  coupon:quantity:7 25 \
  coupon:quantity:8 80 \
  coupon:quantity:9 60 \
  coupon:quantity:10 120 > /dev/null

# 특수 상황 쿠폰들 (테스트용)
redis-cli MSET \
  coupon:quantity:11 5 \
  coupon:quantity:12 1 \
  coupon:quantity:13 0 \
  coupon:quantity:14 3 \
  coupon:quantity:15 85 > /dev/null

echo "  ✅ 일반 쿠폰: 10개 (재고 25~200개)"
echo "  ✅ 특수 쿠폰: 5개 (재고 0~5개, 테스트용)"

echo ""
echo "✅ 데이터 생성 완료!"
echo ""
echo "🔍 확인 명령어:"
echo "  인기상품: redis-cli ZREVRANGE product:rank:$YESTERDAY 0 5 WITHSCORES"
echo "  쿠폰재고: redis-cli MGET coupon:quantity:1 coupon:quantity:5 coupon:quantity:13"
echo "  전체키: redis-cli KEYS '*'"
echo ""
echo "📊 생성된 데이터 요약:"
echo "  • 인기 상품 랭킹: 4일치 데이터"
echo "  • 쿠폰 재고: 15개 쿠폰 (0~200개)"
echo ""
echo "🎯 이제 K6 테스트 실행 준비 완료!"