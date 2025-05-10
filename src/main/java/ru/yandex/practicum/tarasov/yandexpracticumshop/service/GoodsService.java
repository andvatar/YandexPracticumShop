package ru.yandex.practicum.tarasov.yandexpracticumshop.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.*;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.GoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderGoodsRepository;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@Service
public class GoodsService {
    private final GoodsRepository goodsRepository;
    private final OrderGoodsRepository orderGoodsRepository;
    private final OrderService orderService;

    public GoodsService(GoodsRepository goodsRepository,
                        OrderGoodsRepository orderGoodsRepository,
                        OrderService orderService) {
        this.goodsRepository = goodsRepository;
        this.orderGoodsRepository = orderGoodsRepository;
        this.orderService = orderService;
    }

    public Page<Goods> findAll(int page, int size, String sortBy, String order) {
        Pageable pageable = PageRequest.of(page, size, Objects.equals(sortBy, "no") ? Sort.unsorted() : Sort.by(Sort.Direction.fromString(order), sortBy));
        return goodsRepository.findAllByQuantityGreaterThan(0, pageable);
    }

    @Transactional
    public void addRemoveToCart(long goodsId, String action) {

        Order cart = orderService.getCart();

        long orderId = cart.getId();

        Goods goods = goodsRepository.findById(goodsId).orElseThrow(() -> new NoSuchElementException("Goods not found"));
        int goodsQuantity = goods.getQuantity();

        Optional<OrderGoods> orderGoodsOptional = orderGoodsRepository.findByIdOrderIdAndIdGoodsId(orderId, goodsId);

        switch (action) {
            case "plus":
                if(orderGoodsOptional.isPresent()) {
                    addRemoveGoods(orderGoodsOptional.get(), 1, goodsQuantity);
                }
                else {
                    addGoods(goods, cart);
                }
            break;
            case "minus":
                orderGoodsOptional.ifPresent(orderGoods -> addRemoveGoods(orderGoods, -1, goodsQuantity));
            break;
            case "delete":
                orderGoodsOptional.ifPresent(orderGoodsRepository::delete);
            break;
            default:
                throw new NoSuchElementException("Unknown action: " + action);
        }
    }

    private void addGoods(Goods goods, Order cart) {
        OrderGoods orderGoods = new OrderGoods(cart, goods, 1);
        orderGoodsRepository.save(orderGoods);
    }

    private void addRemoveGoods(OrderGoods orderGoods, int amount, int goodsQuantity) {
        int quantityInCart = orderGoods.getQuantity();
        if(quantityInCart + amount > goodsQuantity) {
            throw new NoSuchElementException("Not enough goods in store");
        }
        if(quantityInCart + amount == 0) {
            orderGoodsRepository.delete(orderGoods);
        }
        else {
            orderGoods.setQuantity(orderGoods.getQuantity() + amount);
            orderGoodsRepository.save(orderGoods);
        }
    }
}
