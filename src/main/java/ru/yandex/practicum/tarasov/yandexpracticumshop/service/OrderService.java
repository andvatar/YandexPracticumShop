package ru.yandex.practicum.tarasov.yandexpracticumshop.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Goods;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderStatus;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.GoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final GoodsRepository goodsRepository;

    public OrderService(OrderRepository orderRepository,
                        GoodsRepository goodsRepository) {
        this.orderRepository = orderRepository;
        this.goodsRepository = goodsRepository;
    }

    public Order getOrder(long id) {
        return orderRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    public Order getCart() {
        return orderRepository.findByStatus(OrderStatus.NEW).orElseGet(() -> orderRepository.save(new Order(OrderStatus.NEW)));
    }

    public List<Order> getOrders() {
        return orderRepository.findByStatusNot(OrderStatus.NEW);
    }

    @Transactional
    public long buyCart() {
        Order order = getCart();
        if (order.getGoods().isEmpty()) {
            throw new NoSuchElementException("Cart is empty");
        }
        for(Goods goods:order.items()) {
            int newQuantity = goods.getQuantity() - goods.getCount();
            if(newQuantity < 0) {
                throw new NoSuchElementException("Not enough goods in store: " + goods.getTitle());
            }
            goods.setQuantity(newQuantity);
            goodsRepository.save(goods);
        }
        order.setStatus(OrderStatus.IN_PROGRESS);
        return orderRepository.save(order).getId();
    }
}
