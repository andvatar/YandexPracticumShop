package ru.yandex.practicum.tarasov.yandexpracticumshop.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.yandex.practicum.tarasov.yandexpracticumshop.api.PaymentApi;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Goods;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderGoods;
import ru.yandex.practicum.tarasov.yandexpracticumshop.enums.ErrorMessages;
import ru.yandex.practicum.tarasov.yandexpracticumshop.enums.OrderStatus;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.GoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderGoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.OrderService;

import java.math.BigDecimal;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = OrderService.class)
@ActiveProfiles("test")
@WithMockUser("test")
public class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private GoodsRepository goodsRepository;

    @MockitoBean
    private OrderGoodsRepository orderGoodsRepository;

    @MockitoBean
    private PaymentApi paymentApi;

    private Order order;
    private Goods goods;
    private OrderGoods orderGoods;

    @BeforeEach
    public void init() {
        Mockito.reset(orderRepository, goodsRepository);

        order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.NEW);

        goods = new Goods();
        goods.setId(1L);
        goods.setTitle("Test");
        goods.setDescription("Test");
        goods.setPrice(1000.0);
        goods.setQuantity(10);

        orderGoods = new OrderGoods(order, goods, 5);

        order.getGoods().add(orderGoods);
        goods.getOrderGoods().add(orderGoods);
    }

    @Test
    public void buyEmptyCart() {

        order.getGoods().clear();

        when(orderRepository.findCart(any())).thenReturn(Mono.just(order));
        when(orderGoodsRepository.findByOrderId(any())).thenReturn(Flux.empty());
        when(orderRepository.orderPrice(any())).thenReturn(Mono.just(BigDecimal.valueOf(0)));

        StepVerifier
                .create(orderService.buyCart())
                .expectErrorMatches(throwable ->
                        throwable instanceof NoSuchElementException && throwable.getMessage().equals(ErrorMessages.EMPTY_CART.getMessage()))
                .verify();

        verify(goodsRepository, times(0)).save(any(Goods.class));
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    public void buyMoreGoodsThanWeHave() {
        orderGoods.setQuantity(15);

        when(orderRepository.findCart(any())).thenReturn(Mono.just(order));
        when(orderGoodsRepository.findByOrderId(any())).thenReturn(Flux.just(orderGoods));
        when(goodsRepository.findById(anyLong())).thenReturn(Mono.just(goods));
        when(orderRepository.orderPrice(any())).thenReturn(Mono.just(BigDecimal.valueOf(1000)));

        StepVerifier
                .create(orderService.buyCart())
                .expectErrorMatches(throwable ->
                        throwable instanceof NoSuchElementException && throwable.getMessage().equals(ErrorMessages.NO_GOODS_WITH_TITLE.getMessage() + "Test"))
                .verify();

        verify(goodsRepository, times(0)).save(any(Goods.class));
        verify(orderRepository, times(0)).save(any(Order.class));
    }

    @Test
    public void buyCart() {
        when(orderRepository.findCart(any())).thenReturn(Mono.just(order));
        when(orderGoodsRepository.findByOrderId(any())).thenReturn(Flux.just(orderGoods));
        when(goodsRepository.findById(anyLong())).thenReturn(Mono.just(goods));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> Mono.just(i.getArguments()[0]));
        when(goodsRepository.save(any(Goods.class))).thenAnswer(i -> Mono.just(i.getArguments()[0]));
        when(orderRepository.orderPrice(any())).thenReturn(Mono.just(BigDecimal.valueOf(1000)));
        when(paymentApi.paymentPerformIdPutWithHttpInfo(any(), any())).thenReturn(Mono.just(ResponseEntity.ok().build()));

        StepVerifier
                .create(orderService.buyCart())
                .assertNext(id -> assertEquals(id, order.getId()))
                .verifyComplete();

        verify(goodsRepository, times(1)).save(any(Goods.class));
        verify(orderRepository, times(1)).save(any(Order.class));

        assertEquals(OrderStatus.IN_PROGRESS, order.getStatus());
        assertEquals( 5, goods.getQuantity());
        assertEquals( 5, orderGoods.getQuantity());
    }
}
