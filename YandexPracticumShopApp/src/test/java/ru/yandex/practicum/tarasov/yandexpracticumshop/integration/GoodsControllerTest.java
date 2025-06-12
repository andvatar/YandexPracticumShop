package ru.yandex.practicum.tarasov.yandexpracticumshop.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.yandex.practicum.tarasov.yandexpracticumshop.YandexPracticumShopApplication;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderGoods;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.GoodsService;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.OrderService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = YandexPracticumShopApplication.class)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
public class GoodsControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    @Test
    void getAllGoods() {
        webTestClient.get()
                .uri("/main/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);
    }

    @Test
    @WithMockUser(username = "test_user")
    void addItemToCart() {
        Long id = goodsService.findAll(null, PageRequest.of(0, 100, Sort.unsorted()))
                        .blockFirst().id();

        webTestClient.post()
                .uri("/main/items/{id}", id)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", "plus"))
                .exchange()
                .expectStatus().isSeeOther()
                .expectBody().isEmpty();

        List<OrderGoods> orderGoodsList = orderService.getCartWithItems()
                .map(Order::getGoods)
                .block();

        assertNotNull(orderGoodsList);
        assertEquals(id, orderGoodsList.getFirst().getGoodsId());
        assertEquals(1, orderGoodsList.getFirst().getQuantity());
    }

    @Test
    @WithAnonymousUser
    void anonymousAllGoods() {
        webTestClient.get()
                .uri("/main/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class);
    }

    @Test
    @WithAnonymousUser
    void anonymousAddItemToCart() {
        Long id = goodsService.findAll(null, PageRequest.of(0, 100, Sort.unsorted()))
                .blockFirst().id();

        webTestClient.post()
                .uri("/main/items/{id}", id)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", "plus"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class)
                .isEqualTo("Access Denied");
    }
}
