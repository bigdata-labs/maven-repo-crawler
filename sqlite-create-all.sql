create table links (
  id                            integer not null,
  link                          varchar(1024),
  parent_link                   varchar(1024),
  has_child_link                integer(1),
  md5                           varchar(2048),
  sha1                          varchar(2048),
  level                         integer,
  pom                           TEXT,
  version                       integer not null,
  constraint pk_links primary key (id)
);

