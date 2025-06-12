package ru.yandex.practicum.tarasov.yandexpracticumshop.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.UserDto;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.CustomUserRepository;

@Service
public class UserService {

    private final CustomUserRepository customUserRepository;

    public UserService(CustomUserRepository customUserRepository) {
        this.customUserRepository = customUserRepository;
    }

    @Transactional
    public Mono<Void> registerUser(UserDto userDto) {
        return customUserRepository.createUser(userDto).then(Mono.empty());
    }

    @Transactional
    public Mono<UserDetails> loadUserByUsername(String username) {
        return customUserRepository.getUserByUsername(username).cast(UserDetails.class);
    }
}
