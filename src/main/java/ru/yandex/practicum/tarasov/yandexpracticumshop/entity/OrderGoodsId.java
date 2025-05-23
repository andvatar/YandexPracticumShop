package ru.yandex.practicum.tarasov.yandexpracticumshop.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class OrderGoodsId {
    private long orderId;
    private long goodsId;
}
