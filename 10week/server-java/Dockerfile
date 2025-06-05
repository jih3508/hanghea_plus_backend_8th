FROM eclipse-temurin:17-jre

WORKDIR /app

# 로컬에서 빌드된 JAR 파일만 복사
COPY build/libs/*.jar app.jar

# 1GB RAM 환경에 최적화된 JVM 옵션
ENV JAVA_OPTS="-server \
    -Xms256m \
    -Xmx512m \
    -XX:+UseG1GC \
    -XX:MaxGCPauseMillis=200 \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]