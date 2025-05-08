package ru.yandex.practicum.tarasov.yandexpracticumshop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Columns;
import org.javamoney.moneta.Money;

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
    @GeneratedValue
    private long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "quantity")
    private int quantity;

    @Columns(columns = {
            @Column(name = "price_amount"),
            @Column(name = "price_currency")
    })
    private Money price;

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
                .append("name", name)
                .append("quantity", quantity)
                .append("price", price)
                .toString();
    }
}
