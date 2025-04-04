create table if not exists wlt_account
(
    id            bigint generated by default as identity
        primary key,
    balance       numeric(38, 2),
    created_at    timestamp(6),
    created_by    varchar(255),
    updated_at    timestamp(6),
    updated_by    varchar(255),
    user_id       bigint,
    block_balance numeric(38, 2),
    ccy           varchar(255),
    status        varchar(255)
);

alter table wlt_account
    owner to admin;