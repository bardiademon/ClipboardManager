create table  if not exists "clipboard" (

  "id"         integer primary key autoincrement not null,
  "name"       nvarchar(50),
  "clipboard"  text,
  "created_at" datetime not null default current_timestamp,
  "deleted_at" datetime
);