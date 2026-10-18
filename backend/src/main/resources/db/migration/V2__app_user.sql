-- Restricted user the backend uses at runtime: it can read and change rows,
-- but owns nothing, so it cannot drop, alter or create tables.
create role silkyroad_app login password '${app_password}';
grant usage on schema public to silkyroad_app;
grant select, insert, update, delete on all tables in schema public to silkyroad_app;
revoke all on flyway_schema_history from silkyroad_app;

-- Tables created by later migrations get the same rights automatically
alter default privileges in schema public grant select, insert, update, delete on tables to silkyroad_app;
