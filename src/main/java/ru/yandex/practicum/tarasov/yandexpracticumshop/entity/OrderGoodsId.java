package ru.yandex.practicum.tarasov.yandexpracticumshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class OrderGoodsId {
    @Column(name = "order_id")
    private long orderId;
    @Column(name = "goods_id")
    private long goodsId;
}
