package ru.yandex.practicum.tarasov.yandexpracticumshop.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


@Table(name = "order_goods")
@NoArgsConstructor
@Getter
@Setter
public class OrderGoods {
    @Id
    @Column("id")
    private long id;

    @Column("order_id")
    private long orderId;

    @Column("goods_id")
    private long goodsId;

    @Transient
    private Order order;

    @Transient
    private Goods goods;

    @Column("quantity")
    private int quantity;

    public OrderGoods(Order order, Goods goods, int quantity) {
        if (order == null || goods == null) {
            throw new IllegalArgumentException("Order and Goods must not be null");
        }
        this.order = order;
        this.goods = goods;
        this.quantity = quantity;
        this.orderId = order.getId();
        this.goodsId = goods.getId();
    }
}
