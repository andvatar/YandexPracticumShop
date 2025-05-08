package ru.yandex.practicum.tarasov.yandexpracticumshop.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "order_goods")
public class OrderGoods {
    @EmbeddedId
    private OrderGoodsId id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "goods_id")
    private Goods goods;

    @Column(name = "quantity")
    private int quantity;
}
