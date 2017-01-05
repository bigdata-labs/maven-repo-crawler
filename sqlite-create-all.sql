create table links (
  id                            integer not null,
  link                          varchar(1024),
  parent_link                   varchar(1024),
  path_type                     varchar(1024),
  level                         integer,
  version                       integer not null,
  constraint pk_links primary key (id)
);

