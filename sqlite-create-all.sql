create table links (
  id                            integer not null,
  link                          varchar(1024),
  parent_link                   varchar(1024),
  path_type                     varchar(1024),
  level                         integer,
  version                       integer not null,
  constraint pk_links primary key (id)
);

create table next_links (
  id                            integer not null,
  link                          varchar(2048),
  crawled                       integer(1),
  version                       integer not null,
  constraint pk_next_links primary key (id)
);

