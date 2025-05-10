package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderGoods;

import java.util.Optional;

@Repository
public interface OrderGoodsRepository extends JpaRepository<OrderGoods, Long> {

    Optional<OrderGoods> findByIdOrderIdAndIdGoodsId(Long orderId, Long goodsId);


}
