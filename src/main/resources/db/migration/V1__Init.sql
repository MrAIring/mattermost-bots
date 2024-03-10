create table commands
(
    id      serial primary key,
    trigger text not null unique
        constraint chk_trigger_length check ( char_length(trigger) <= 30 ),
    token   text not null
        constraint chk_token_length check ( char_length(token) <= 30 )
);

create table groups
(
    id    serial primary key,
    mm_id text not null unique
        constraint chk_mm_id_length check ( char_length(mm_id) <= 30 ),
    token text not null
        constraint chk_token_length check ( char_length(token) <= 30 ),
    name  text not null unique
        constraint chk_name_length check ( char_length(name) <= 50 )
);

create table groups_users
(
    group_id   int  not null references groups (id) on delete cascade,
    user_mm_id text not null
        constraint chk_user_mm_id_length check ( char_length(user_mm_id) <= 30 ),
    primary key (group_id, user_mm_id)
);

create table groups_groups
(
    group_id          int not null references groups (id) on delete cascade,
    included_group_id int not null references groups (id) on delete cascade,
    primary key (group_id, included_group_id)
);

create table groups_default_channels
(
    group_id      int  not null references groups (id) on delete cascade,
    channel_mm_id text not null
        constraint chk_channel_mm_id_length check ( char_length(channel_mm_id) <= 30 ),
    primary key (group_id, channel_mm_id)
);

create table on_duty
(
    keyword    text not null
        constraint chk_keyword_length check ( char_length(keyword) <= 255 ),
    user_mm_id text not null
        constraint chk_user_mm_id_length check ( char_length(user_mm_id) <= 30 ),
    primary key (keyword, user_mm_id)
)