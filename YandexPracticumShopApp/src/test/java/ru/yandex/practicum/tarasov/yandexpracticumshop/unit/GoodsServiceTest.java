package ru.yandex.practicum.tarasov.yandexpracticumshop.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Goods;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderGoods;
import ru.yandex.practicum.tarasov.yandexpracticumshop.enums.ErrorMessages;
import ru.yandex.practicum.tarasov.yandexpracticumshop.enums.OrderStatus;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.GoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderGoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.GoodsService;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.OrderService;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = GoodsService.class)
@ActiveProfiles("test")
public class GoodsServiceTest {

    @Autowired
    private GoodsService goodsService;

    @MockitoBean
    private GoodsRepository goodsRepository;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private OrderGoodsRepository orderGoodsRepository;

    @MockitoBean
    private CacheManager cacheManager;

    private Order order;
    private Goods goods;

    @BeforeEach
    public void init() {
        Mockito.reset(orderGoodsRepository, goodsRepository);

        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.NEW);

        goods = new Goods();
        goods.setId(1L);
        goods.setTitle("Test");
        goods.setDescription("Test");
        goods.setPrice(1000.0);
        goods.setQuantity(10);
    }

    @Test
    public void addToCartWhenOnGoodsAvailable() {
        goods.setQuantity(0);
        when(orderService.getCart()).thenReturn(Mono.just(order));
        when(goodsRepository.findById(1L)).thenReturn(Mono.just(goods));
        when(orderGoodsRepository.findByOrderIdAndGoodsId(1L, 1L)).thenReturn(Mono.empty());

        StepVerifier
                .create(goodsService.addRemoveToCart(1L, "plus"))
                .expectErrorMatches(throwable ->
                    throwable instanceof NoSuchElementException && throwable.getMessage().equals(ErrorMessages.NO_GOODS.getMessage())
                )
                .verify();
    }

    @Test
    public void addToCart() {
        when(orderService.getCart()).thenReturn(Mono.just(order));
        when(goodsRepository.findById(1L)).thenReturn(Mono.just(goods));
        when(orderGoodsRepository.findByOrderIdAndGoodsId(1L, 1L)).thenReturn(Mono.empty());
        when(orderGoodsRepository.save(any())).thenAnswer(i -> Mono.just(i.getArguments()[0]));



        StepVerifier
                .create(goodsService.addRemoveToCart(1L, "plus"))
                .verifyComplete();

        verify(orderGoodsRepository, times(1)).save(any(OrderGoods.class));
    }

    @Test
    public void minusFromCart() {
        OrderGoods orderGoods = new OrderGoods(order, goods, 5);

        order.getGoods().add(orderGoods);
        goods.getOrderGoods().add(orderGoods);

        when(orderService.getCart()).thenReturn(Mono.just(order));
        when(goodsRepository.findById(1L)).thenReturn(Mono.just(goods));
        when(orderGoodsRepository.findByOrderIdAndGoodsId(1L, 1L)).thenReturn(Mono.just(orderGoods));
        when(orderGoodsRepository.save(any())).thenAnswer(i -> Mono.just(i.getArguments()[0]));

        StepVerifier
                .create(goodsService.addRemoveToCart(1L, "minus"))
                .verifyComplete();

        verify(orderGoodsRepository, times(1)).save(any(OrderGoods.class));
        assertEquals(4, orderGoods.getQuantity());
    }

    @Test
    public void removeFromCart() {
        OrderGoods orderGoods = new OrderGoods(order, goods, 1);

        order.getGoods().add(orderGoods);
        goods.getOrderGoods().add(orderGoods);

        when(orderService.getCart()).thenReturn(Mono.just(order));
        when(goodsRepository.findById(1L)).thenReturn(Mono.just(goods));
        when(orderGoodsRepository.findByOrderIdAndGoodsId(1L, 1L)).thenReturn(Mono.just(orderGoods));
        when(orderGoodsRepository.delete(any())).thenReturn(Mono.empty());

        StepVerifier
                .create(goodsService.addRemoveToCart(1L, "minus"))
                .verifyComplete();

        verify(orderGoodsRepository, times(1)).delete(any(OrderGoods.class));
    }
}
