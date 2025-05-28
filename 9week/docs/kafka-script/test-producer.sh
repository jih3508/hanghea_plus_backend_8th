#!/bin/bash

# Kafka Producer 테스트 스크립트

echo "Testing Kafka Producer..."
echo "Type messages and press Enter. Type 'exit' to quit."

docker exec -it kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic order-events
