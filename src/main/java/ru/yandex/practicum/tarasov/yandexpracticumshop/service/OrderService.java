package ru.yandex.practicum.tarasov.yandexpracticumshop.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderStatus;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderRepository;

import java.util.NoSuchElementException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order getOrder(long id) {
        return orderRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    public Order getCart() {
        return orderRepository.findByStatus(OrderStatus.NEW).orElseGet(() -> orderRepository.save(new Order(OrderStatus.NEW)));
    }
}
