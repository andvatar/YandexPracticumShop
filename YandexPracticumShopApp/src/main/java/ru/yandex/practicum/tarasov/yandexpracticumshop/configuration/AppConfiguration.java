package ru.yandex.practicum.tarasov.yandexpracticumshop.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.tarasov.yandexpracticumshop.ApiClient;
import ru.yandex.practicum.tarasov.yandexpracticumshop.api.PaymentApi;

@Configuration
public class AppConfiguration {
    @Bean
    public PaymentApi paymentApi(WebClient.Builder oauth2WebClientBuilder) {
        return new PaymentApi(new ApiClient(oauth2WebClientBuilder.build()));
    }
}
