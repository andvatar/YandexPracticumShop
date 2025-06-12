package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.UserDto;

public interface CustomUserRepository {
    Mono<UserDto> createUser(UserDto userDto);
    Mono<UserDto> getUserByUsername(String username);
}
