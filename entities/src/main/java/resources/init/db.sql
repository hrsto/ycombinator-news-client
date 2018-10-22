create table if not exists message(
    id bigint primary key,
    version bigint,
    created timestamp not null,
    lastUpdated timestamp not null,
    author varchar(50) not null,
);
create table if not exists article(
    id bigint primary key,
    version bigint,
    score int not null,
    title clob not null,
    descendants bigint not null,
    url blob,
    read boolean not null,
    foreign key (id) references message(id) on update cascade on delete cascade
);
create table if not exists comment(
    id bigint primary key,
    version bigint,
    parent bigint not null,
    text clob not null,
    foreign key (id) references message(id) on update cascade on delete cascade,
    foreign key (parent) references message(id),
);
