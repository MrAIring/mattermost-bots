create table commands
(
    id      int auto_increment primary key,
    trigger varchar(30) not null unique,
    token   varchar(30) not null
);

create table groups
(
    id    int auto_increment primary key,
    mm_id varchar(30) not null unique,
    token varchar(30) not null,
    name  varchar(50) not null unique
);

create table groups_users
(
    group_id   int         not null references groups (id) on delete cascade,
    user_mm_id varchar(30) not null,
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
    group_id      int         not null references groups (id) on delete cascade,
    channel_mm_id varchar(30) not null,
    primary key (group_id, channel_mm_id)
);
