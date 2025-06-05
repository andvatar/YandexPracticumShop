package ru.yandex.practicum.tarasov.payment.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.api.PaymentApi;

import java.math.BigDecimal;

@RestController
public class PaymentApiImpl implements PaymentApi {

    @Value("${custom.availableSum}")
    private BigDecimal availableSum;

    @Override
    public Mono<ResponseEntity<BigDecimal>> paymentBalanceIdGet(Integer id, ServerWebExchange exchange) {

        return Mono.just(ResponseEntity.ok(availableSum));
    }

    @Override
    public Mono<ResponseEntity<String>> paymentPerformIdPut(Integer id, Mono<BigDecimal> body, ServerWebExchange exchange) {
        return body.flatMap(sum -> sum.compareTo(availableSum) > 0 ?
                Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not enough money on the account")) :
                Mono.just(ResponseEntity.ok("Ok")));
    }
}
