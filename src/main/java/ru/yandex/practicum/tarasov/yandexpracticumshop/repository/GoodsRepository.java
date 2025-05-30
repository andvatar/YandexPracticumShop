package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.ItemDTO;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Goods;

@Repository
public interface GoodsRepository extends ReactiveCrudRepository<Goods, Long>, CustomGoodsRepository {
    @Query("""
            select g.*, coalesce(og.quantity,0) as "count"
            from goods g
            left outer join order_goods og on g.id = og.goods_id and og.order_id = :order_id
            where g.id = :id""")
    Mono<ItemDTO> findDTOById(@Param("id") Long id, @Param("order_id") long orderId);

    @Query("""
            select g.*, og.quantity as "count"
            from goods g
            join order_goods og on g.id = og.goods_id
            where og.order_id = :order_id""")
    Flux<ItemDTO> findAllDTOByOrderId(@Param("order_id") long orderId);

    Mono<Goods> findByTitle(String title);
}
