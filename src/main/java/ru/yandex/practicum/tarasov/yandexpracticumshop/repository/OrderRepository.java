package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.OrderDTO;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderStatus;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    @Query("select id, status from orders where status = 'NEW'")
    Mono<Order> findCart();
    @NotNull
    @Query("select id, status from orders where id = :id")
    Mono<Order> findById(@NotNull @Param("id") Long orderId);
    @Query("select id, status from orders where status <> 'NEW'")
    Flux<OrderDTO> findOrders();

    /*@Query("select id, status from orders where status = 'NEW'")
    Mono<OrderDTO> findCartDTO();
    @Query("select id, status from orders where id = :id")
    Mono<OrderDTO> findDTOById(@Param("id") Long orderId);
    @Query("select id, status from orders where status <> 'NEW'")
    Flux<OrderDTO> findOrdersDTO();*/
}
