-- Insert test users
INSERT INTO user (id, name, user_id, create_date_time) 
VALUES (1, 'Test User 1', 'testuser1', NOW()),
       (2, 'Test User 2', 'testuser2', NOW());

-- Insert points for users
INSERT INTO point (id, user_id, point)
VALUES (1, 1, 10000),
       (2, 2, 5000);

-- Insert products
INSERT INTO product (id, name, product_number, price, category)
VALUES (1, 'Test Product 1', 'P001', 1000, 'FOOD'),
       (2, 'Test Product 2', 'P002', 2000, 'ELECTRONIC_DEVICES'),
       (3, 'Test Product 3', 'P003', 3000, 'ETC');

-- Insert product stock
INSERT INTO product_stock (id, product_id, quantity)
VALUES (1, 1, 10),
       (2, 2, 5),
       (3, 3, 20);

-- Insert coupons
INSERT INTO coupon (id, coupon_number, quantity, type, rate, discount_price, start_date_time, end_date_time)
VALUES (1, 'FLAT500', 10, 'FLAT', NULL, 500, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
       (2, 'RATE10', 5, 'RATE', 10, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY));

-- Insert user coupons
INSERT INTO user_coupon (id, user_id, coupon_id, is_used)
VALUES (1, 1, 1, 0);

-- Insert product ranks
INSERT INTO product_rank (id, product_id, rank_date, `rank`)
VALUES (1, 1, CURDATE(), 1),
       (2, 2, CURDATE(), 2),
       (3, 3, CURDATE(), 3);
