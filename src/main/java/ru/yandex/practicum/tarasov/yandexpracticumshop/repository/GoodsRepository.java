package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Goods;

import java.util.Optional;

@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {
    Page<Goods> findAllByQuantityGreaterThan(int quantity, Pageable pageable);
    Page<Goods> findAllByTitleOrDescription(String name, String description, Pageable pageable);
    Optional<Goods> findByTitle(String title);

}
