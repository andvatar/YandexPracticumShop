package ru.yandex.practicum.tarasov.yandexpracticumshop.DTO;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import ru.yandex.practicum.tarasov.yandexpracticumshop.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        @Id
        @Column("id")
        long id,
        @Column("status")
        OrderStatus status,

        List<ItemDto> items
) {
        public BigDecimal totalSum() {
                return items()
                        .stream()
                        .map(item -> item.price().multiply(BigDecimal.valueOf(item.count())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
}
