
drop table if exists `user` cascade;
drop table if exists `point` cascade;
drop table if exists `point_history` cascade;

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
)
    comment '포인트';


create table point_history
(
    id               bigint auto_increment comment ' pk'
        primary key,
    user_id          bigint                             not null comment '사용자 FK',
    type             enum ('CHARGE', 'USE')             not null comment '충전 || 사용',
    amount           decimal  default 0                 not null comment '충전 및 사용 금액',
    create_date_time datetime default CURRENT_TIMESTAMP not null comment '생성일시'
)
    comment '포인트 이력';

