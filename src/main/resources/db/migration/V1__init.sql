-- User service schema v1: accounts (profile + credentials), their dietary
-- restrictions and favourites, and the advisory restriction catalogue.
-- recipe_id / ingredient_id values reference other services by value - no FKs
-- across the boundary. Matches the JPA mappings; Hibernate runs validate.

create table account (
    id            bigint       not null,
    username      varchar(50)  not null,
    email         varchar(255) not null,
    display_name  varchar(100) not null,
    password_hash varchar(100) not null,
    created_at    datetime(6)  not null,
    updated_at    datetime(6)  not null,
    primary key (id),
    constraint uk_account_username unique (username),
    constraint uk_account_email unique (email)
) engine = InnoDB;

create table account_restriction (
    account_id       bigint       not null,
    restriction_code varchar(100) not null,
    primary key (account_id, restriction_code),
    constraint fk_account_restriction__account foreign key (account_id) references account (id)
) engine = InnoDB;

create table account_favorite_recipe (
    account_id bigint not null,
    recipe_id  bigint not null,
    primary key (account_id, recipe_id),
    constraint fk_account_favorite_recipe__account foreign key (account_id) references account (id)
) engine = InnoDB;

create table favorite_alternative (
    id                        bigint not null,
    account_id                bigint not null,
    recipe_id                 bigint not null,
    ingredient_id             bigint not null,
    replacement_ingredient_id bigint not null,
    primary key (id),
    constraint uk_favorite_alternative unique (account_id, recipe_id, ingredient_id),
    constraint fk_favorite_alternative__account foreign key (account_id) references account (id)
) engine = InnoDB;

create table restriction (
    id          bigint       not null,
    code        varchar(100) not null,
    label       varchar(100) not null,
    kind        varchar(40)  not null,
    description varchar(500),
    primary key (id),
    constraint uk_restriction_code unique (code)
) engine = InnoDB;

create index ix_restriction_kind on restriction (kind);

-- @GeneratedValue (AUTO) on MySQL: one id table per entity.
create table account_seq (next_val bigint) engine = InnoDB;
insert into account_seq (next_val) values (1);
create table favorite_alternative_seq (next_val bigint) engine = InnoDB;
insert into favorite_alternative_seq (next_val) values (1);
create table restriction_seq (next_val bigint) engine = InnoDB;
insert into restriction_seq (next_val) values (1);
