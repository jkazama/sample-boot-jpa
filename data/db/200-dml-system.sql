truncate table app_setting cascade;
truncate table audit_actor cascade;
truncate table audit_event cascade;

insert into app_setting (id, setting_value) values ('system.businessDay.day', to_char(current_date, 'YYYY-MM-DD'));
