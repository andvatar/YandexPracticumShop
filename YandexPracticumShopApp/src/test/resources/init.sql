delete from order_goods;
delete from orders;
delete from goods;
delete from users;
insert into goods (title, description, quantity, price_amount) values ('test', 'test', 10, 1000.0);
insert into users (username, password, enabled) values ('test_user', 'test_password', true);
insert into users (username, password, enabled) values ('admin', 'admin_password', true);
insert into orders (status, username) values ('NEW', 'test_user');


