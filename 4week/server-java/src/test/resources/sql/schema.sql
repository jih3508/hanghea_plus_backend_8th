drop table if exists `product_rank` cascade;

drop table if exists `order_product_history` cascade;

drop table if exists `order_item` cascade;
drop table if exists `order` cascade;

drop table if exists `product_stock` cascade;
drop table if exists `product` cascade;

drop table if exists `point_history` cascade;
drop table if exists `point` cascade;

drop table if exists `coupon` cascade;

drop table if exists `user` cascade;

drop table if exists `user_coupon` cascade;



create table user
(
    id               BIGINT auto_increment comment 'PK'
        primary key,
    name             VARCHAR(500)           not null comment '이름'
        unique,
    user_id          VARCHAR(500)           not null comment '사용자 ID',
    create_date_time datetime default NOW() not null comment '생성 일시',
    update_date_time datetime               null comment '수정일시'
) comment '사용자';


create table point
(
    id      BIGINT auto_increment
        primary key,
    user_id BIGINT            not null comment '사용자 pk',
    point   NUMERIC default 0 not null comment '포인트',
    constraint point__user_fk
        foreign key (user_id) references user (id)
) comment '포인트';


create table point_history
(
    id               bigint auto_increment comment ' pk'
        primary key,
    user_id          bigint                             not null comment '사용자 FK',
    type             enum ('CHARGE', 'USE')             not null comment '충전 || 사용',
    amount           decimal  default 0                 not null comment '충전 및 사용 금액',
    create_date_time datetime default CURRENT_TIMESTAMP not null comment '생성일시',
    constraint point_history_user_fk
        foreign key (user_id) references user (id)
            on update cascade on delete cascade
) comment '포인트 이력';


create table product
(
    id                bigint auto_increment comment 'PK'
        primary key,
    name              varchar(500)                               not null comment '상품명',
    product_number    varchar(500)                               not null comment '상품 번호',
    price             decimal  default 0                         not null comment '가격',
    category          enum ('FOOD', 'ELECTRONIC_DEVICES', 'ETC') not null comment '카테코리',
    created_date_time datetime default CURRENT_TIMESTAMP         not null comment '생성 일시',
    updated_date_time datetime                                   null comment '수정 일시',
    constraint product_number
        unique (product_number)
) comment '상품';


create table product_stock
(
    id         BIGINT auto_increment comment 'PK',
    product_id BIGINT        not null comment '상품 FK',
    quantity   int default 0 not null comment '수량',
    constraint product_stock_pk
        unique (id),
    constraint product_stock_product_fk
        foreign key (product_id) references product (id)
)
    comment '상품 재고';


create table coupon
(
    id                bigint auto_increment comment 'PK'
        primary key,
    coupon_number     varchar(500)                       not null comment '쿠폰 번호',
    quantity          int      default 0                 not null comment '수량',
    type              enum ('FLAT', 'RATE')              null comment '정액 || 정률',
    rate              int                                null comment '할인률',
    discount_price    decimal                            null comment '할인금액',
    start_date_time   datetime                           not null comment '만료기간 (시작)',
    end_date_time     datetime                           not null comment '만료기간 (종료)',
    created_date_time datetime default CURRENT_TIMESTAMP not null comment '생성일시',
    updated_date_time datetime                           null comment '수정 일시'
)
    comment '쿠폰';

create table user_coupon
(
    id               bigint auto_increment comment 'pk'
        primary key,
    user_id          bigint                               not null comment '유저 FK',
    coupon_id        bigint                               not null comment '쿠폰 FK',
    is_used          tinyint(1) default 1                 not null comment '사용여부',
    issued_date_time datetime   default CURRENT_TIMESTAMP not null comment '발급 일시'
)
    comment '사용자 쿠폰';

create table `order`
(
    id                bigint auto_increment comment 'PK'
        primary key,
    user_id           bigint                             not null comment '유저 fk',
    order_number      varchar(100)                       not null comment '주문 번호',
    total_price       decimal                            not null comment '주문 총 금액',
    discount_price    decimal                            null comment '할인 금액',
    created_date_time datetime default CURRENT_TIMESTAMP not null comment '생성 일시',
    updated_date_time datetime                           null
)
    comment '주문';


create table order_item
(
    id          bigint auto_increment comment 'PK'
        primary key,
    order_id    bigint  not null comment '주문 FK',
    product_id  bigint  not null comment '상품 fk',
    total_price decimal not null comment '상품 총 가격'
)
    comment '주문 상품';

create table order_product_history
(
    id         bigint auto_increment comment 'PK'
        primary key,
    order_id   bigint null comment '주문 FK',
    product_id bigint not null comment '상품 fk',
    quantity   int    not null comment '수량',
    create_date_time datetime default CURRENT_TIMESTAMP not null
)
    comment '주문 상품 이력';

create table product_rank
(
    id         bigint auto_increment comment 'PK'
        primary key,
    product_id bigint not null comment '상품 fk',
    rank_date  date   not null,
    `rank`     int    not null comment '순위',
    constraint product_rank_product_fk
        foreign key (product_id) references product (id)
)
    comment '상품 랭킹 이력';


