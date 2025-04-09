### ERD
```mermaid
erDiagram
    USER ||--|| POINT: ""
    USER ||--o{ POINT_HISTORY: ""
    USER |o--o{ USER_COUPON: ""
    USER |o--o{ COUPON_HISTORY: ""
    USER |o--o{ ORDER: ""
    
    COUPON |o--o{ USER_COUPON: ""
    
    PRODUCT ||--|| PRODUCT_STOCK: ""
    PRODUCT |o--o{ PRODUCT_RANK: ""
    PRODUCT |o--o{ ORDER_ITEM:""
    
    ORDER ||--o{ ORDER_ITEM:""
    
    USER {
        BIGINT id PK "유저 PK"
        VARCHAR user_id "유저 아이디"
        VARCHAR name "이름"
        DATETIME created_date_time "생성 일시"
        DATETIME updated_date_time "수정 일시"
    }

    POINT {
        BIGINT id PK "잔고 PK"
        BIGINT user_id FK "유저 id"
        DECIMAL amount "잔액"
    }

    POINT_HISTORY{
        BIGINT id PK 
        BIGINT user_id FK "유저 fk"
        ENUM type "충저 || 사용"
        DECIMAL amount "충전 및 사용 금액"
        DATETIME created_date_time "생성 일시"
    }

    COUPON{
        BIGINT id PK 
        VARCHAR coupon_number "쿠폰 번호"
        INTEGER quantity "수량"
        ENUM type "정액 || 정률"
        INTEGER rate "할인률"
        INTEGER discount_price "할인 금액"
        DATETIME start_date_time "만료기간 (시작)"
        DATETIME end_date_time "만료기간 (종료)"
        DATETIME created_date_time "생성 일시"
        DATETIME updated_date_time "수정 일시"
    }
    
    USER_COUPON{
        BIGINT user_id FK  "유저 fk"
        BIGINT coupon_id FK  "쿠폰 fk"
        BOOLEAN is_used "사용 여부"
        DATETIME issued_date_time "발급 일시"
    }

    COUPON_HISTORY{
        BIGINT id PK
        BIGINT user_id FK "유저 fk"
        BIGINT coupon_id FK "쿠폰 fk"
        ENUM type "발급 || 사용"
        DATETIME created_date_time "발급 일시"
    }

    PRODUCT{
        BIGINT id PK
        VARCHAR name "상품명"
        VARCHAR procuct_number "상품 번호"
        DECIMAL price "가격"
        ENUM category "상품 종류"
        DATETIME created_date_time "생성 일시"
        DATETIME updated_date_time "수정 일시"
    }

    PRODUCT_STOCK{
        BIGINT id PK
        BIGINT product_id FK "상품 fk"
        INTEGER quantity "재고 수량"
    }
    
    ORDER{
        BIGINT id PK
        BIGINT user_id FK "유저 fk"
        BIGINT coupon_id FK "쿠폰 fk"
        VARCHAR order_number "주문 번호"
        DECIMAL total_price "주문 총 금액"
        DECIMAL discount_price "할인 금액"
        DATETIME created_date_time "생성 일시"
        DATETIME updated_date_time "수정 일시"
    }
    
    ORDER_ITEM{
        BIGINT id PK
        BIGINT order_id FK "주문 fk"
        BIGINT product_id FK "상품 fk"
        DECIMAL total_price "상품 총 가격"
        INTEGER quantity  "수량"
    }
    

    PRODUCT_RANK{
        BIGINT id PK
        BIGINT product_id FK "상품 fk"
        DATE rank_date "순위 저장 일자"
        INTEGER rank "순위"
        INTEGER quantity "상품 주문 총 개수"
    }

```

### 테이블 목록
|name| description |
|----|-------------|
|`USER`| 사용자         |
|`POINT`| 사용자 잔고      |
|`POINT_HISTORY`| 사용자 잔고 이력   |
|`COUPON`| 쿠폰          |
|`USER_COUPON`| 사용자 보유 쿠폰   |
|`PRODUCT`| 상품          |
|`PRODUCT_STOCK`| 재고          |
|`ORDER`| 주문          |
|`ORDER_ITEM`| 주문 항목       |
|`ORDER_PRODUCT_HISTORY`| 상품 주문 이력    |
|`PRODUCT_RANK`| 상품 순위       |
