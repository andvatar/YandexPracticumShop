package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.ItemDTO;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Goods;

@Repository
public interface GoodsRepository extends ReactiveCrudRepository<Goods, Long>, CustomGoodsRepository {
    /*@Query("""
            select g.*, coalesce(og.quantity,0) as "count"
            from goods g
            left outer join order_goods og on g.id = og.goods_id and og.order_id = :order_id
            where g.quantity > 0
            order by
                case when :sortBy = 'title' and :orderDirection = 'ASC' then g.title end,
                case when :sortBy = 'price' and :orderDirection = 'ASC' then g.price_amount end,
                case when :sortBy = 'title' and :orderDirection = 'DESC' then g.title end DESC,
                case when :sortBy = 'price' and :orderDirection = 'ASC' then g.price_amount end DESC,
                case when :sortBy = 'no' then g.id end""")
    Flux<ItemDTO> findAllDTO(@Param("order_id") long orderId, Pageable pageable, @Param("sortBy") String sortBy, @Param(":orderDirection") String orderDirection);

    @Query("""
            select g.*, coalesce(og.quantity,0) as "count"
            from goods g
            left outer join order_goods og on g.id = og.goods_id and og.order_id = :order_id
            where g.quantity > 0
            and (title like :name or description like :name)""")
    Flux<ItemDTO> findAllDTOByTitle(@Param("name") String name, @Param("order_id") long orderId, Pageable pageable, String sortBy, String orderDirection);*/

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

    @Query("select count(*) from goods where quantity > 0")
    Mono<Long> countByQuantityGreaterThanZero();

    @Query("select count(*) from goods where quantity > 0 and (title like :name or description like :name)")
    Mono<Long> countByTitleOrDescription(String name);
}
