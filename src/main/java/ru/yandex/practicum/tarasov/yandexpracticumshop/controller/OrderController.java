package ru.yandex.practicum.tarasov.yandexpracticumshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Goods;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.OrderGoods;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.OrderService;

import java.util.List;

@Controller
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }


    @RequestMapping("/cart/items")
    public String getCart(Model model) {
        Order order = orderService.getCart();
        List<Goods> goods = order.getGoods().stream().map(OrderGoods::getGoods).toList();
        double totalSum = goods
                .stream()
                .map(g -> g
                        .getPrice() * g.getCount())
                .reduce(Double::sum).orElse(0.0);
        boolean empty = order.getGoods().isEmpty();
        model.addAttribute("items", goods);
        model.addAttribute("total", totalSum);
        model.addAttribute("empty", empty);
        return "cart";
    }
}
