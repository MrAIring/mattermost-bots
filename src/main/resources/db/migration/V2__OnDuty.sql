create table on_duty
(
    keyword    varchar(255) not null,
    user_mm_id varchar(30)  not null,
    primary key (keyword, user_mm_id)
)