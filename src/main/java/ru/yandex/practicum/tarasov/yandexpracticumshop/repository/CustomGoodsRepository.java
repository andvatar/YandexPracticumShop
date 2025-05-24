package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.ItemDTO;

public interface CustomGoodsRepository {
    Mono<Page<ItemDTO>> findAllDTOByTitle(String search, long orderId, Pageable pageable, String sortBy, String order);
}
