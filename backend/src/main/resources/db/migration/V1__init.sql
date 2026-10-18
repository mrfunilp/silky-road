create table users (
    id            uuid                        not null,
    username      varchar(20)                 not null,
    email         varchar(255)                not null,
    password_hash varchar(255)                not null,
    created_at    timestamp(6) with time zone not null,
    primary key (id),
    constraint uk_users_username unique (username),
    constraint uk_users_email unique (email)
);

create table marketplace_items (
    id         uuid                        not null,
    name       varchar(120)                not null,
    biography  varchar(2000),
    creator_id uuid                        not null,
    price      numeric(12, 2)              not null,
    currency   varchar(3)                  not null,
    category   varchar(60),
    status     varchar(20)                 not null,
    created_at timestamp(6) with time zone not null,
    updated_at timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_marketplace_items_creator foreign key (creator_id) references users (id),
    constraint ck_marketplace_items_status check (status in ('DRAFT', 'ACTIVE', 'SOLD', 'ARCHIVED'))
);

create table marketplace_item_images (
    item_id   uuid not null,
    image_url varchar(500),
    constraint fk_marketplace_item_images_item foreign key (item_id) references marketplace_items (id)
);

create table item_comments (
    id         uuid                        not null,
    item_id    uuid                        not null,
    author_id  uuid                        not null,
    text       varchar(500)                not null,
    created_at timestamp(6) with time zone not null,
    primary key (id),
    constraint fk_item_comments_item foreign key (item_id) references marketplace_items (id),
    constraint fk_item_comments_author foreign key (author_id) references users (id)
);

create index ix_marketplace_items_creator on marketplace_items (creator_id);
create index ix_marketplace_items_status on marketplace_items (status);
create index ix_marketplace_item_images_item on marketplace_item_images (item_id);
create index ix_item_comments_item on item_comments (item_id);
