drop table if exists app_setting cascade;
drop table if exists audit_actor cascade;
drop table if exists audit_event cascade;
drop sequence if exists audit_actor_id_seq;
drop sequence if exists audit_event_id_seq;

create table app_setting (id varchar(120) not null, category varchar(60), outline varchar(1300), setting_value varchar(1300) not null, primary key (id));

create sequence audit_actor_id_seq start 10000;
create table audit_actor (id bigint not null default nextval('audit_actor_id_seq'), actor_id varchar(30) not null, category varchar(30) not null, end_date timestamp(6), error_reason varchar(400), message varchar(400), role_type varchar(30) not null, source varchar(128), start_date timestamp(6) not null, status_type varchar(30) not null, time bigint, primary key (id));

create sequence audit_event_id_seq start 10000;
create table audit_event (id bigint not null default nextval('audit_event_id_seq'), category varchar(30), end_date timestamp(6), error_reason varchar(400), message varchar(400), start_date timestamp(6) not null, status_type varchar(30) not null, time bigint, primary key (id));
