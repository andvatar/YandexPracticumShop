package ru.yandex.practicum.tarasov.yandexpracticumshop.service;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.OrderDto;
import ru.yandex.practicum.tarasov.yandexpracticumshop.api.PaymentApi;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
import ru.yandex.practicum.tarasov.yandexpracticumshop.enums.ErrorMessages;
import ru.yandex.practicum.tarasov.yandexpracticumshop.enums.OrderStatus;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.GoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderGoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderRepository;

import java.util.NoSuchElementException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;
    private final OrderGoodsRepository orderGoodsRepository;
    private final PaymentApi paymentApi;

    public OrderService(OrderRepository orderRepository,
                        GoodsRepository goodsRepository,
                        OrderGoodsRepository orderGoodsRepository,
                        PaymentApi paymentApi)
                         {
        this.orderRepository = orderRepository;
        this.goodsRepository = goodsRepository;
        this.orderGoodsRepository = orderGoodsRepository;
        this.paymentApi = paymentApi;
    }

    @PreAuthorize("isAuthenticated()")
    public Mono<OrderDto> getOrderDTO(long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException(ErrorMessages.ORDER_NOT_FOUND.getMessage() + id)))
                .flatMap(this::orderToDTO);
    }

    public Mono<OrderDto> getCartDTO() {
        return isAuthenticated().flatMap(a -> {
            if(a) {
                return getUsername().flatMap(orderRepository::findCart)
                .switchIfEmpty(Mono.defer(() -> orderRepository.save(new Order(OrderStatus.NEW))))
                .flatMap(this::orderToDTO);
            }
            else {
                return Mono.empty();
            }
        });
    }

    @PreAuthorize("isAuthenticated()")
    public Mono<Order> getCart() {
        return getUsername().flatMap(orderRepository::findCart)
                .switchIfEmpty(Mono.defer(() -> orderRepository.save(new Order(OrderStatus.NEW))));
    }

    @PreAuthorize("isAuthenticated()")
    public Mono<Order> getCartWithItems() {
        return getUsername().flatMap(orderRepository::findCart)
                .switchIfEmpty(Mono.defer(() -> orderRepository.save(new Order(OrderStatus.NEW))))
                .flatMap(cart -> orderGoodsRepository.findByOrderId(cart.getId())
                        .collectList()
                        .map(og -> {
                            cart.setGoods(og);
                            return cart;
                        }));
    }

    @PreAuthorize("isAuthenticated()")
    public Flux<Order> getOrders() {
        return getUsername().flatMapMany(orderRepository::findOrders);
    }

    @PreAuthorize("isAuthenticated()")
    public Flux<OrderDto> getOrdersDTO() {
        return getUsername()
                .flatMapMany(orderRepository::findOrders)
                .flatMap(this::orderToDTO);
    }

    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Mono<Long> buyCart() {
        return getCartWithItems()
                .flatMap(order -> Flux.fromIterable(order.getGoods())
                        .switchIfEmpty(Mono.error(new NoSuchElementException(ErrorMessages.EMPTY_CART.getMessage())))
                        .flatMap(orderGoods ->
                                goodsRepository.findById(orderGoods.getGoodsId())
                                        .flatMap(goods -> {
                                            if (orderGoods.getQuantity() > goods.getQuantity()) {
                                                return Mono.error(new NoSuchElementException(ErrorMessages.NO_GOODS_WITH_TITLE.getMessage() + goods.getTitle()));
                                            } else {
                                                goods.setQuantity(goods.getQuantity() - orderGoods.getQuantity());
                                                return goodsRepository.save(goods);
                                            }
                                        })
                        )
                        .then(orderRepository.orderPrice(order.getId())
                                .flatMap(price -> paymentApi.paymentPerformIdPutWithHttpInfo(1, price))
                        )
                        .flatMap(entity -> entity.getStatusCode().is2xxSuccessful() ? Mono.empty() : Mono.error(new IllegalArgumentException(entity.getBody())))
                        .then(Mono.defer(() -> {
                            order.setStatus(OrderStatus.IN_PROGRESS);
                            return orderRepository.save(order);
                        })))
                .map(Order::getId);
    }

    private Mono<OrderDto> orderToDTO(Order order) {
        return goodsRepository.findAllDTOByOrderId(order.getId())
                .collectList()
                .map(items ->
                        new OrderDto(order.getId(), order.getStatus(), items)
                );
    }

    private Mono<Boolean> isAuthenticated() {
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .map(Authentication::isAuthenticated)
                .defaultIfEmpty(false);
    }

    private Mono<String> getUsername() {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getName());
    }
}
