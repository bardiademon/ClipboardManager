create table if not exists "clipboard" (

  "id"         integer primary key autoincrement not null,
  "name"       nvarchar(50),
  "data"       text,
  "type"       varchar(50) not null,
  "created_at" datetime not null default current_timestamp,
  "deleted_at" datetime
);