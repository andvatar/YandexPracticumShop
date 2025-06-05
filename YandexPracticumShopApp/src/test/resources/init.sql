delete from order_goods;
delete from orders;
delete from goods;
insert into goods (title, description, quantity, price_amount) values ('test', 'test', 10, 1000.0);
insert into orders (status) values ('NEW');
