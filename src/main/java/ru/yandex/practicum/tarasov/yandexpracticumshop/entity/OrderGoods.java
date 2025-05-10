package ru.yandex.practicum.tarasov.yandexpracticumshop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_goods")
@NoArgsConstructor
@Getter
@Setter
public class OrderGoods {
    @EmbeddedId
    private OrderGoodsId id;

    @ManyToOne
    @MapsId("orderId")
    private Order order;

    @ManyToOne
    @MapsId("goodsId")
    private Goods goods;

    @Column(name = "quantity")
    private int quantity;

    public OrderGoods(Order order, Goods goods, int quantity) {
        if (order == null || goods == null) {
            throw new IllegalArgumentException("Order and Goods must not be null");
        }
        this.order = order;
        this.goods = goods;
        this.quantity = quantity;
        this.id = new OrderGoodsId(order.getId(), goods.getId());
    }
}
