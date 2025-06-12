package ru.yandex.practicum.tarasov.yandexpracticumshop.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.yandex.practicum.tarasov.yandexpracticumshop.enums.OrderStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Order {
    @Id
    @Column("id")
    private long id;

    @Column("status")
    private OrderStatus status;

    @Column("username")
    private String username;

    @Transient
    private List<OrderGoods> goods = new ArrayList<>();

    public Order(OrderStatus status, String username) {
        this.status = status;
        this.username = username;
    }

    public List<Goods> items() {
        return goods.stream().map(OrderGoods::getGoods).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("status", status)
                .toString();
    }
}
