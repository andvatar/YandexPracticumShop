package ru.yandex.practicum.tarasov.yandexpracticumshop.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "goods")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Goods {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "img_path")
    private String imgPath;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "price_amount")
    private Double price;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "goods")
    private List<OrderGoods> orderGoods = new ArrayList<>();

    public int getCount() {
        return orderGoods
                .stream()
                .filter(og -> og.getOrder().getStatus().equals(OrderStatus.NEW))
                .findFirst()
                .map(OrderGoods::getQuantity)
                .orElse(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goods goods = (Goods) o;
        return id == goods.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", title)
                .append("description", description)
                .append("imgPath", imgPath)
                .append("quantity", quantity)
                .append("price", price)
                .toString();
    }
}
