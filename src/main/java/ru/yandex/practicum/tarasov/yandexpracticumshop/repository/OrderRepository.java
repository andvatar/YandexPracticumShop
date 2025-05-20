package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderStatus;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
    Mono<Order> findByStatus(OrderStatus status);
    Flux<Order> findByStatusNot(OrderStatus status);
}
