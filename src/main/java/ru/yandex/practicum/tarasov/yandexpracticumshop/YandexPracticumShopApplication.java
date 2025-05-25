package ru.yandex.practicum.tarasov.yandexpracticumshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@SpringBootApplication
@EnableR2dbcRepositories(basePackages = "ru.yandex.practicum.tarasov.yandexpracticumshop.repository")
public class YandexPracticumShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(YandexPracticumShopApplication.class, args);
    }
}
