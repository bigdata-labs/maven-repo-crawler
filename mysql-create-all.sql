create table links (
  id                            bigint auto_increment not null,
  link                          varchar(1024),
  parent_link                   varchar(1024),
  path_type                     varchar(1024),
  group_id                      varchar(128),
  artifact_id                   varchar(50),
  artifact_version              varchar(40),
  version                       bigint not null,
  constraint pk_links primary key (id)
);

create table pom_content (
  id                            bigint auto_increment not null,
  content                       longtext,
  link                          varchar(2048),
  version                       bigint not null,
  constraint pk_pom_content primary key (id)
);

