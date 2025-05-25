package ru.yandex.practicum.tarasov.yandexpracticumshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.OrderService;

@Controller
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @RequestMapping("/cart/items")
    public Mono<String> getCart(Model model) {
        return orderService.getCartDTO()
                        .map(order -> {
                            model.addAttribute("items", order.items());
                            model.addAttribute("total", order.totalSum());
                            model.addAttribute("empty", order.items().isEmpty());
                            return "cart";
                        });
    }

    @RequestMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return orderService.getOrdersDTO()
                .collectList()
                .doOnNext(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }

    @PostMapping("/buy")
    public Mono<String> buyItems() {
        return orderService.buyCart()
                        .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true");

    }

    @RequestMapping("/orders/{id}")
    public Mono<String> getOrder(Model model,
                                    @PathVariable("id") int orderId) {

        return orderService.getOrderDTO(orderId)
                .map(order -> {
                    model.addAttribute("order", order);
                    return "order";}
                );
    }
}
