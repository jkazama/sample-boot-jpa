truncate table account cascade;
truncate table cash_balance cascade;
truncate table cashflow cascade;
truncate table cash_in_out cascade;
truncate table fi_account cascade;
truncate table holiday cascade;
truncate table login cascade;
truncate table self_fi_account cascade;
truncate table staff cascade;
truncate table staff_authority cascade;

insert into self_fi_account (category, currency, fi_code, fi_account_id) values ('CashOut', 'USD', 'CashOut-USD', 'xxxxxx');
insert into staff (staff_id, name, role_type) values ('admin', 'admin', 3);
insert into account (account_id, name, mail_address, status_type) values ('sample', 'sample', 'sample@example.com', 0);
insert into login (actor_id, role_type, login_id, password) values ('admin', 3, 'admin', '$2a$04$gvIxTocwYk/JsLloGN6IB.IHijxtKo.Bl9XsTeJPbe07ytMsKrPCW');
insert into login (actor_id, role_type, login_id, password) values ('sample', 1, 'sample', '$2a$04$T8e4NZ2SuaJFcRP/9wwo5e9GkJXUejaZA8GcZ.rgyWX.6Hqs5RZPa');
insert into fi_account (account_id, category, currency, fi_code, fi_account_id) values ('sample', 'CashOut', 'USD', 'CashOut-USD', 'FIsample');
insert into cash_balance (account_id, base_day, currency, amount, update_date) values ('sample', current_date, 'USD', '10000', current_timestamp);
