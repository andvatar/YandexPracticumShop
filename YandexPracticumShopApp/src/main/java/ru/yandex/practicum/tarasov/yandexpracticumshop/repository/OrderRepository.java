package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;

import java.math.BigDecimal;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    @Query("select id, status from orders where status = 'NEW' and username = :username")
    Mono<Order> findCart(@Param("username") String username);
    @Query("select id, status from orders where id = :id")
    Mono<Order> findById(@Param("id") Long orderId);
    @Query("select id, status from orders where status <> 'NEW' and username = :username")
    Flux<Order> findOrders(@Param("username") String username);

    @Query("""
            select sum(og.quantity * g.price_amount)
            from orders o
            left outer join order_goods og on o.id = og.order_id
            left outer join goods g on g.id = og.goods_id
            where o.id = :order_id
            """)
    Mono<BigDecimal> orderPrice(@Param("id") Long orderId);
}
