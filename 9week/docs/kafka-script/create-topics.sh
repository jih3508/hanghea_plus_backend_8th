#!/bin/bash

# Kafka 토픽 생성 스크립트

echo "Creating Kafka topics..."

# 주문 이벤트 토픽
docker exec kafka kafka-topics --create \
  --topic order-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# 결제 이벤트 토픽  
docker exec kafka kafka-topics --create \
  --topic payment-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# 재고 이벤트 토픽
docker exec kafka kafka-topics --create \
  --topic inventory-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# 알림 이벤트 토픽
docker exec kafka kafka-topics --create \
  --topic notification-events \
  --bootstrap-server localhost:9092 \
  --partitions 2 \
  --replication-factor 1

echo "Topics created successfully!"

# 토픽 리스트 확인
echo "Current topics:"
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
