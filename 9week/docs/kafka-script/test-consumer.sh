#!/bin/bash

# Kafka Consumer 테스트 스크립트

echo "Testing Kafka Consumer..."
echo "Listening for messages from order-events topic..."

docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning
