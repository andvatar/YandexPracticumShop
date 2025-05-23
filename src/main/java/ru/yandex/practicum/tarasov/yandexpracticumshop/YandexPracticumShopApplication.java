package ru.yandex.practicum.tarasov.yandexpracticumshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.GoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderGoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderRepository;

@SpringBootApplication
@EnableR2dbcRepositories(basePackageClasses = {GoodsRepository.class, OrderGoodsRepository.class, OrderRepository.class})
public class YandexPracticumShopApplication {

    public static void main(String[] args) {
        SpringApplication.run(YandexPracticumShopApplication.class, args);
    }
}
