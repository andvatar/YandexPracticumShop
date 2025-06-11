package ru.yandex.practicum.tarasov.yandexpracticumshop.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import ru.yandex.practicum.tarasov.yandexpracticumshop.api.PaymentApi;

@Configuration
public class AppConfiguration {
    @Bean
    @Primary
    public PaymentApi paymentApi() {
        return new PaymentApi();
    }
}
