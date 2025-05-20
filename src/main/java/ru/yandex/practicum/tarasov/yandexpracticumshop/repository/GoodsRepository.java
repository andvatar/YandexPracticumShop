package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Goods;

@Repository
public interface GoodsRepository extends ReactiveCrudRepository<Goods, Long>, ReactiveSortingRepository<Goods, Long> {
    Flux<Goods> findAllByQuantityGreaterThan(int quantity, Pageable pageable);
    Flux<Goods> findAllByTitleOrDescription(String name, String description, Pageable pageable);
    Mono<Goods> findByTitle(String title);
    Mono<Long> countByQuantityGreaterThan(int quantity);
    Mono<Long> countByTitleOrDescription(String name, String description);

}
