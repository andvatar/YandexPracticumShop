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
public interface GoodsRepository extends ReactiveCrudRepository<Goods, Long>, ReactiveSortingRepository<Goods, Long> {
    @Query("""
               select g.*, og.quantity as "count"
               from goods g
               left outer join order_goods og on g.id = og.goods_id
               left outer join orders o on o.id = og.order_id
               where g.quantity > 0 and o.status = 'NEW'""")
    Flux<ItemDTO> findAllDTO(Pageable pageable);

    @Query("""
               select g.*, og.quantity as "count"
               from goods g
               left outer join order_goods og on g.id = og.goods_id
               left outer join orders o on o.id = og.order_id
               where g.quantity > 0 and o.status = 'NEW'
			   and (title like :name or description like :name)""")
    Flux<ItemDTO> findAllDTOByTitle(@Param("name") String name, Pageable pageable);

    @Query("""
               select g.*, og.quantity as "count"
               from goods g
               left outer join order_goods og on g.id = og.goods_id
               left outer join orders o on o.id = og.order_id
               where g.id = :id and o.status = 'NEW'""")
    Mono<ItemDTO> findDTOById(@Param("id") Long id);

    Mono<Goods> findByTitle(String title);

    @Query("select count(*) from goods where quantity > 0")
    Mono<Long> countByQuantityGreaterThanZero();
    @Query("select count(*) from goods where quantity > 0 and (title like :name or description like :name)")
    Mono<Long> countByTitleOrDescription(String name);
}
