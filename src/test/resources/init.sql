drop table if exists tenant;
create table tenant
(
    id          bigint auto_increment comment '主键'
        primary key,
    name        varchar(200)                       not null comment '租户名称',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime                           null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint unique_tenant_name_idx
        unique (name)
);
drop table if exists tenant_data;
create table tenant_data
(
    id          bigint auto_increment comment '主键'
        primary key,
    tid         bigint                             not null comment '租户id',
    some_text   varchar(100)                       null comment '租户数据',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime                           null on update CURRENT_TIMESTAMP comment '更新时间'
);

create index tid_idx
    on tenant_data (tid, some_text);

-- insert test data
INSERT INTO tenant (name) VALUES ('测试租户1');
INSERT INTO tenant (name) VALUES ('测试租户2');

INSERT INTO tenant_data (tid, some_text) VALUES (1, 'somedata1');
INSERT INTO tenant_data (tid, some_text) VALUES (1, 'somedata2');
INSERT INTO tenant_data (tid, some_text) VALUES (1, 'somedata3');
INSERT INTO tenant_data (tid, some_text) VALUES (2, 'somedata4');
INSERT INTO tenant_data (tid, some_text) VALUES (2, 'somedata5');
INSERT INTO tenant_data (tid, some_text) VALUES (2, 'somedata6');



