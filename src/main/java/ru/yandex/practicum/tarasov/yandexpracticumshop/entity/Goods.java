package ru.yandex.practicum.tarasov.yandexpracticumshop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
//import jakarta.persistence.*;


import lombok.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Table(name = "goods")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Goods {
    @Id
    @Column("id")
    private long id;

    @JsonProperty("title")
    @Column("title")
    private String title;

    @JsonProperty("description")
    @Column("description")
    private String description;

    @JsonProperty("img_path")
    @Column("img_path")
    private String imgPath;

    @JsonProperty("quantity")
    @Column("quantity")
    private int quantity;

    @JsonProperty("price_amount")
    @Column("price_amount")
    private Double price;

    @Transient
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
