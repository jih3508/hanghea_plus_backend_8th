SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE product_rank;
TRUNCATE TABLE order_product_history;
TRUNCATE TABLE order_item;
TRUNCATE TABLE `order`;
TRUNCATE TABLE user_coupon;
TRUNCATE TABLE product_stock;
TRUNCATE TABLE product;
TRUNCATE TABLE point_history;
TRUNCATE TABLE point;
TRUNCATE TABLE coupon;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. 사용자 데이터 생성 (1,000명)
DELIMITER $$
CREATE PROCEDURE GenerateUsers()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE user_name VARCHAR(500);
    DECLARE user_id_val VARCHAR(500);

    WHILE i <= 1000 DO
            SET user_name = CONCAT('테스트사용자', LPAD(i, 4, '0'));
            SET user_id_val = CONCAT('user', LPAD(i, 3, '0'));

INSERT INTO hhplus.user (hhplus.user.name, hhplus.user.user_id, hhplus.user.create_date_time)
VALUES (user_name, user_id_val, NOW() - INTERVAL FLOOR(RAND() * 365) DAY);

SET i = i + 1;
END WHILE;
END$$
DELIMITER ;

CALL GenerateUsers();
DROP PROCEDURE GenerateUsers;

-- 2. 상품 데이터 생성 (100개)
INSERT INTO product (product.name, product.product_number, product.price, product.category, product.created_date_time) VALUES
-- FOOD 카테고리 (40개)
('신선한 사과 1kg', 'FOOD001', 8900, 'FOOD', NOW()),
('유기농 바나나 1송이', 'FOOD002', 4500, 'FOOD', NOW()),
('프리미엄 쌀 10kg', 'FOOD003', 35000, 'FOOD', NOW()),
('국내산 배추 1포기', 'FOOD004', 3200, 'FOOD', NOW()),
('신선한 계란 30구', 'FOOD005', 7800, 'FOOD', NOW()),
('등심 스테이크 300g', 'FOOD006', 28000, 'FOOD', NOW()),
('자연산 고등어 1마리', 'FOOD007', 12000, 'FOOD', NOW()),
('유기농 우유 1L', 'FOOD008', 3800, 'FOOD', NOW()),
('수제 요거트 500ml', 'FOOD009', 5200, 'FOOD', NOW()),
('프리미엄 빵 세트', 'FOOD010', 15000, 'FOOD', NOW()),
('특급 김치 1kg', 'FOOD011', 18000, 'FOOD', NOW()),
('무농약 당근 1kg', 'FOOD012', 4200, 'FOOD', NOW()),
('신선한 토마토 1kg', 'FOOD013', 6500, 'FOOD', NOW()),
('국내산 감자 3kg', 'FOOD014', 8900, 'FOOD', NOW()),
('유기농 양파 2kg', 'FOOD015', 5600, 'FOOD', NOW()),
('프리미엄 올리브오일', 'FOOD016', 25000, 'FOOD', NOW()),
('수제 간장 500ml', 'FOOD017', 12000, 'FOOD', NOW()),
('천연 꿀 300g', 'FOOD018', 18000, 'FOOD', NOW()),
('무첨가 고추장 1kg', 'FOOD019', 15000, 'FOOD', NOW()),
('신선한 생선회 세트', 'FOOD020', 45000, 'FOOD', NOW()),
('국내산 한우 갈비 1kg', 'FOOD021', 85000, 'FOOD', NOW()),
('자연산 새우 500g', 'FOOD022', 35000, 'FOOD', NOW()),
('유기농 블루베리 200g', 'FOOD023', 8900, 'FOOD', NOW()),
('프리미엄 견과류 세트', 'FOOD024', 22000, 'FOOD', NOW()),
('수제 치즈 300g', 'FOOD025', 18000, 'FOOD', NOW()),
('신선한 샐러드 세트', 'FOOD026', 12000, 'FOOD', NOW()),
('국내산 닭가슴살 1kg', 'FOOD027', 15000, 'FOOD', NOW()),
('유기농 시금치 300g', 'FOOD028', 3500, 'FOOD', NOW()),
('프리미엄 파스타 면', 'FOOD029', 8500, 'FOOD', NOW()),
('수제 소시지 500g', 'FOOD030', 16000, 'FOOD', NOW()),
('신선한 딸기 1팩', 'FOOD031', 12000, 'FOOD', NOW()),
('국내산 콩나물 500g', 'FOOD032', 2800, 'FOOD', NOW()),
('유기농 브로콜리 1개', 'FOOD033', 4500, 'FOOD', NOW()),
('프리미엄 참기름', 'FOOD034', 28000, 'FOOD', NOW()),
('수제 된장 1kg', 'FOOD035', 18000, 'FOOD', NOW()),
('신선한 연어 스테이크', 'FOOD036', 32000, 'FOOD', NOW()),
('국내산 돼지고기 앞다리', 'FOOD037', 18000, 'FOOD', NOW()),
('유기농 당근 주스', 'FOOD038', 6500, 'FOOD', NOW()),
('프리미엄 마늘 1kg', 'FOOD039', 15000, 'FOOD', NOW()),
('수제 김 20매', 'FOOD040', 8800, 'FOOD', NOW()),

-- ELECTRONIC_DEVICES 카테고리 (35개)
('스마트폰 갤럭시 Pro', 'ELEC001', 1200000, 'ELECTRONIC_DEVICES', NOW()),
('노트북 맥북 에어', 'ELEC002', 1450000, 'ELECTRONIC_DEVICES', NOW()),
('태블릿 아이패드', 'ELEC003', 850000, 'ELECTRONIC_DEVICES', NOW()),
('블루투스 이어폰', 'ELEC004', 180000, 'ELECTRONIC_DEVICES', NOW()),
('무선 키보드', 'ELEC005', 120000, 'ELECTRONIC_DEVICES', NOW()),
('게이밍 마우스', 'ELEC006', 85000, 'ELECTRONIC_DEVICES', NOW()),
('모니터 27인치 4K', 'ELEC007', 650000, 'ELECTRONIC_DEVICES', NOW()),
('스마트 워치', 'ELEC008', 380000, 'ELECTRONIC_DEVICES', NOW()),
('휴대용 배터리', 'ELEC009', 45000, 'ELECTRONIC_DEVICES', NOW()),
('USB-C 충전기', 'ELEC010', 35000, 'ELECTRONIC_DEVICES', NOW()),
('웹캠 HD 화질', 'ELEC011', 95000, 'ELECTRONIC_DEVICES', NOW()),
('블루투스 스피커', 'ELEC012', 150000, 'ELECTRONIC_DEVICES', NOW()),
('게이밍 헤드셋', 'ELEC013', 200000, 'ELECTRONIC_DEVICES', NOW()),
('SSD 1TB', 'ELEC014', 180000, 'ELECTRONIC_DEVICES', NOW()),
('RAM 16GB', 'ELEC015', 120000, 'ELECTRONIC_DEVICES', NOW()),
('그래픽카드 RTX', 'ELEC016', 800000, 'ELECTRONIC_DEVICES', NOW()),
('스마트 TV 55인치', 'ELEC017', 1200000, 'ELECTRONIC_DEVICES', NOW()),
('에어팟 프로', 'ELEC018', 320000, 'ELECTRONIC_DEVICES', NOW()),
('아이폰 14 Pro', 'ELEC019', 1350000, 'ELECTRONIC_DEVICES', NOW()),
('닌텐도 스위치', 'ELEC020', 380000, 'ELECTRONIC_DEVICES', NOW()),
('플레이스테이션 5', 'ELEC021', 650000, 'ELECTRONIC_DEVICES', NOW()),
('XBOX 시리즈 X', 'ELEC022', 620000, 'ELECTRONIC_DEVICES', NOW()),
('드론 DJI', 'ELEC023', 450000, 'ELECTRONIC_DEVICES', NOW()),
('액션캠 고프로', 'ELEC024', 380000, 'ELECTRONIC_DEVICES', NOW()),
('프린터 레이저', 'ELEC025', 280000, 'ELECTRONIC_DEVICES', NOW()),
('라우터 공유기', 'ELEC026', 150000, 'ELECTRONIC_DEVICES', NOW()),
('외장하드 2TB', 'ELEC027', 85000, 'ELECTRONIC_DEVICES', NOW()),
('USB 허브', 'ELEC028', 35000, 'ELECTRONIC_DEVICES', NOW()),
('스마트폰 거치대', 'ELEC029', 25000, 'ELECTRONIC_DEVICES', NOW()),
('무선 충전패드', 'ELEC030', 45000, 'ELECTRONIC_DEVICES', NOW()),
('캠코더 4K', 'ELEC031', 580000, 'ELECTRONIC_DEVICES', NOW()),
('전자사전', 'ELEC032', 180000, 'ELECTRONIC_DEVICES', NOW()),
('미니 프로젝터', 'ELEC033', 320000, 'ELECTRONIC_DEVICES', NOW()),
('VR 헤드셋', 'ELEC034', 450000, 'ELECTRONIC_DEVICES', NOW()),
('스마트 홈 허브', 'ELEC035', 250000, 'ELECTRONIC_DEVICES', NOW()),

-- ETC 카테고리 (25개)
('프리미엄 백팩', 'ETC001', 120000, 'ETC', NOW()),
('여행용 캐리어', 'ETC002', 180000, 'ETC', NOW()),
('운동화 나이키', 'ETC003', 150000, 'ETC', NOW()),
('청바지 리바이스', 'ETC004', 95000, 'ETC', NOW()),
('화장품 세트', 'ETC005', 85000, 'ETC', NOW()),
('향수 샤넬', 'ETC006', 180000, 'ETC', NOW()),
('시계 롤렉스', 'ETC007', 2500000, 'ETC', NOW()),
('지갑 구찌', 'ETC008', 350000, 'ETC', NOW()),
('선글라스 레이밴', 'ETC009', 220000, 'ETC', NOW()),
('모자 MLB', 'ETC010', 45000, 'ETC', NOW()),
('책 베스트셀러', 'ETC011', 15000, 'ETC', NOW()),
('문구용품 세트', 'ETC012', 25000, 'ETC', NOW()),
('인테리어 소품', 'ETC013', 65000, 'ETC', NOW()),
('식물 화분 세트', 'ETC014', 35000, 'ETC', NOW()),
('캔들 아로마', 'ETC015', 28000, 'ETC', NOW()),
('텀블러 스타벅스', 'ETC016', 35000, 'ETC', NOW()),
('요가매트', 'ETC017', 45000, 'ETC', NOW()),
('덤벨 세트', 'ETC018', 80000, 'ETC', NOW()),
('자전거 헬멧', 'ETC019', 85000, 'ETC', NOW()),
('등산 스틱', 'ETC020', 55000, 'ETC', NOW()),
('캠핑 의자', 'ETC021', 75000, 'ETC', NOW()),
('쿨러백', 'ETC022', 45000, 'ETC', NOW()),
('우산 고급형', 'ETC023', 35000, 'ETC', NOW()),
('안마기', 'ETC024', 180000, 'ETC', NOW()),
('공기청정기', 'ETC025', 250000, 'ETC', NOW());

-- 3. 상품 재고 데이터 생성
INSERT INTO product_stock (product_stock.product_id, product_stock.quantity)
SELECT product.id,
       CASE
           WHEN product.category = 'FOOD' THEN FLOOR(RAND() * 500) + 100
           WHEN product.category = 'ELECTRONIC_DEVICES' THEN FLOOR(RAND() * 50) + 10
           ELSE FLOOR(RAND() * 100) + 20
           END as quantity
FROM product;

-- 4. 포인트 데이터 생성
INSERT INTO point (point.user_id, point.point)
SELECT user.id, FLOOR(RAND() * 100000) + 10000
FROM user;

-- 5. 쿠폰 데이터 생성
INSERT INTO coupon (coupon.coupon_number, coupon.quantity, coupon.type, coupon.rate, coupon.discount_price, coupon.start_date_time, coupon.end_date_time, coupon.created_date_time) VALUES
                                                                                                                                                                                        ('WELCOME5000', 1000, 'FLAT', NULL, 5000, '2024-01-01 00:00:00', '2025-12-31 23:59:59', NOW()),
                                                                                                                                                                                        ('FLAT10000', 500, 'FLAT', NULL, 10000, '2024-01-01 00:00:00', '2025-12-31 23:59:59', NOW()),
                                                                                                                                                                                        ('NEWBIE3000', 2000, 'FLAT', NULL, 3000, '2024-01-01 00:00:00', '2025-12-31 23:59:59', NOW()),
                                                                                                                                                                                        ('SPECIAL20000', 100, 'FLAT', NULL, 20000, '2024-06-01 00:00:00', '2025-06-30 23:59:59', NOW()),
                                                                                                                                                                                        ('SUMMER15000', 300, 'FLAT', NULL, 15000, '2024-07-01 00:00:00', '2025-08-31 23:59:59', NOW()),
                                                                                                                                                                                        ('RATE10', 1500, 'RATE', 10, NULL, '2024-01-01 00:00:00', '2025-12-31 23:59:59', NOW()),
                                                                                                                                                                                        ('RATE20', 800, 'RATE', 20, NULL, '2024-01-01 00:00:00', '2025-12-31 23:59:59', NOW()),
                                                                                                                                                                                        ('RATE15', 1000, 'RATE', 15, NULL, '2024-01-01 00:00:00', '2025-12-31 23:59:59', NOW()),
                                                                                                                                                                                        ('VIP30', 200, 'RATE', 30, NULL, '2024-01-01 00:00:00', '2025-12-31 23:59:59', NOW()),
                                                                                                                                                                                        ('PREMIUM25', 300, 'RATE', 25, NULL, '2024-01-01 00:00:00', '2025-12-31 23:59:59', NOW());

-- 통계 확인
SELECT '=== 테스트 데이터 생성 완료 ===' as status;

SELECT 'Users' as table_name, COUNT(*) as count FROM user
UNION ALL SELECT 'Products', COUNT(*) FROM product
          UNION ALL SELECT 'Product Stock', COUNT(*) FROM product_stock
          UNION ALL SELECT 'Points', COUNT(*) FROM point
          UNION ALL SELECT 'Coupons', COUNT(*) FROM coupon;

SELECT '=== 부하 테스트 준비 완료! ===' as message;
