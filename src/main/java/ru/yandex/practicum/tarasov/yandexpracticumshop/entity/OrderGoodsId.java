package ru.yandex.practicum.tarasov.yandexpracticumshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@Embeddable
@EqualsAndHashCode
public class OrderGoodsId {
    @Column(name = "order_id")
    private long orderId;
    @Column(name = "goods_id")
    private long goodsId;
}
