package ru.yandex.practicum.tarasov.yandexpracticumshop.DTO;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("goods")
public record ItemDto(
        @Id
        @Column("id")
        Long id,
        @Column("title")
        String title,
        @Column("description")
        String description,
        @Column("price_amount")
        BigDecimal price,
        @Column("count")
        Integer count,
        @Column("img_path")
        String imgPath) {
}
