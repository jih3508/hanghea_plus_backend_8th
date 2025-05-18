-- 테스트 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS hhplus_test;

-- 테스트 데이터베이스 사용
USE hhplus_test;

-- 사용자 생성
CREATE USER 'test'@'%' IDENTIFIED BY 'test';

-- 모든 데이터베이스에 대한 권한 부여 (필요에 따라 조정 가능)
GRANT ALL PRIVILEGES ON *.* TO 'test'@'%';

-- 특정 데이터베이스(예: hhplus_test)에만 권한 부여
GRANT ALL PRIVILEGES ON hhplus_test.* TO 'test'@'%';
FLUSH PRIVILEGES;