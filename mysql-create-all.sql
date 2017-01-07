create table links (
  id                            bigint auto_increment not null,
  link                          varchar(1024),
  parent_link                   varchar(1024),
  path_type                     varchar(1024),
  level                         integer,
  version                       bigint not null,
  constraint pk_links primary key (id)
);

create table next_links (
  id                            bigint auto_increment not null,
  link                          varchar(2048),
  crawled                       tinyint(1) default 0,
  version                       bigint not null,
  constraint pk_next_links primary key (id)
);

