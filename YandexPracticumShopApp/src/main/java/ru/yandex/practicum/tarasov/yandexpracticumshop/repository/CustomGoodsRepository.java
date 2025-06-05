package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.ItemDTO;

public interface CustomGoodsRepository {
    Flux<ItemDTO> findAllDTOByTitle(String search, long orderId, Pageable pageable);
}
