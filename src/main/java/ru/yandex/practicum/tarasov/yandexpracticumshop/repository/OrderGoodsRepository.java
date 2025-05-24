package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderGoods;


@Repository
public interface OrderGoodsRepository extends ReactiveCrudRepository<OrderGoods, Long> {

    Mono<OrderGoods> findByOrderIdAndGoodsId(Long orderId, Long goodsId);
    Flux<OrderGoods> findByOrderId(Long orderId);
    Flux<OrderGoods> findByGoodsId(Long goodsId);
}
