create table user_record
(
    user_id  varchar(20) not null comment '用户id'
        primary key,
    username varchar(20) null comment '用户昵称',
    password varchar(20) null comment '用户密码'
)
    comment '用户信息表';

create table chat_record
(
    id              int auto_increment comment '单条聊天记录id'
        primary key,
    sender_id       varchar(20) not null comment '发送方用户id',
    receiver_id     varchar(20) not null comment '接收方用户id',
    message_type    varchar(20) not null comment '消息类型',
    message_content text        null,
    create_at       bigint      not null comment '记录发送时间',
    size            int         null,
    constraint chat_record_user_record_user_id_fk
        foreign key (sender_id) references user_record (user_id),
    constraint chat_record_user_record_user_id_fk_2
        foreign key (receiver_id) references user_record (user_id)
)
    comment '聊天记录表';

create table friend_record
(
    id        int auto_increment comment '一个好友关系记录的id'
        primary key,
    user_id   varchar(20) not null comment '主用户id',
    friend_id varchar(20) not null comment '好友的id',
    constraint friend_record_user_record_user_id_fk
        foreign key (user_id) references user_record (user_id),
    constraint friend_record_user_record_user_id_fk_2
        foreign key (friend_id) references user_record (user_id)
)
    comment '记录好友的表，一对一的关系';


