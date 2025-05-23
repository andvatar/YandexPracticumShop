package ru.yandex.practicum.tarasov.yandexpracticumshop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.OrderDTO;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderStatus;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.GoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderGoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderRepository;

import java.util.ArrayList;
import java.util.NoSuchElementException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;
    private final OrderGoodsRepository orderGoodsRepository;

    public OrderService(OrderRepository orderRepository,
                        GoodsRepository goodsRepository, OrderGoodsRepository orderGoodsRepository) {
        this.orderRepository = orderRepository;
        this.goodsRepository = goodsRepository;
        this.orderGoodsRepository = orderGoodsRepository;
    }

    public Mono<OrderDTO> getOrderDTO(long id) {
        return orderRepository.findById(id)
                .flatMap(this::fetchItems)
                .flatMap(order -> new OrderDTO(order.getId(), order.getStatus(), order.items()))
                .switchIfEmpty(Mono.error(new NoSuchElementException("No order found with id: " + id)));
    }

    public Mono<OrderDTO> getCartDTO() {
        return orderRepository.findCartDTO()
                .switchIfEmpty(Mono.defer(() -> orderRepository.save(new Order(OrderStatus.NEW))
                        .map(cart -> new OrderDTO(cart.getId(), cart.getStatus(), new ArrayList<>()))));
    }

    public Mono<Order> getCart() {
        return orderRepository.findCart()
                .switchIfEmpty(Mono.defer(() -> orderRepository.save(new Order(OrderStatus.NEW))));
    }

    public Flux<Order> getOrders() {
        return orderRepository.findByStatusNot(OrderStatus.NEW);
    }

    public Flux<OrderDTO> getOrdersDTO() {
        return orderRepository.findOrdersDTO();
    }

    @Transactional
    public Mono<Long> buyCart() {
        return getCart()
                .switchIfEmpty(Mono.error(new NoSuchElementException("The cart is empty")))
                .flatMap(order -> Flux.fromIterable(order.items())
                        .flatMap(goods -> {
                                    int newQuantity = goods.getQuantity() - goods.getCount();
                                    if (newQuantity < 0) {
                                        return Mono.error(new NoSuchElementException("Not enough goods in the store: " + goods.getTitle()));
                                    }
                                    goods.setQuantity(newQuantity);
                                    return goodsRepository.save(goods);
                                }
                            )
                            .then(Mono.defer(() -> {
                                order.setStatus(OrderStatus.IN_PROGRESS);
                                return orderRepository.save(order);
                            })))
                .map(Order::getId);
    }

    private Mono<Order> fetchItems(Order order) {
        return orderGoodsRepository.findByOrderId(order.getId())
                .collectList()
                .map(items -> {
                    order.setGoods(items);
                    return order;}
                );
    }
}
