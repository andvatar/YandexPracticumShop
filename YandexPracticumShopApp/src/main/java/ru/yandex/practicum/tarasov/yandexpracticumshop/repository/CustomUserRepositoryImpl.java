package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.UserDto;

import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final DatabaseClient dbClient;
    private final PasswordEncoder passwordEncoder;

    public CustomUserRepositoryImpl(DatabaseClient dbClient,
                                    PasswordEncoder passwordEncoder) {
        this.dbClient = dbClient;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserDto> createUser(UserDto userDto) {

        return dbClient.sql("insert into users (username, password, enabled) values (:username, :password, :enabled)")
                .bind("username", userDto.getUsername())
                .bind("password", passwordEncoder.encode(userDto.getPassword()))
                .bind("enabled", true)
                .fetch()
                .rowsUpdated()
                .then(
                        dbClient.sql("insert into authorities (username, authority) values (:username, :authority)")
                                .bind("username", userDto.getUsername())
                                .bind("authority", "ROLE_USER")
                                .fetch()
                                .rowsUpdated()
                                .then(Mono.just(userDto))
                );
    }

    @Override
    public Mono<UserDto> getUserByUsername(String username) {

        Mono<Set<GrantedAuthority>> authoritiesMono = dbClient.sql("select * from authorities where username = :username")
                .bind("username", username)
                .map((row, metadata) -> new SimpleGrantedAuthority(row.get("authority", String.class)))
                .all()
                .cast(GrantedAuthority.class)
                .collect(Collectors.toSet());

        Mono<UserDto> userMono = dbClient.sql("select * from users where username = :username")
                .bind("username", username)
                .map((row, metadata) -> {
                    UserDto dto = new UserDto();
                    dto.setUsername(row.get("username", String.class));
                    dto.setPassword(row.get("password", String.class));
                    return dto;
                })
                .one();

        return Mono.zip(userMono, authoritiesMono)
                .map(tuple -> {
                     UserDto user = tuple.getT1();
                     Set<GrantedAuthority> authorities = tuple.getT2();
                     user.setAuthorities(authorities);
                     return user;
                });
    }
}
