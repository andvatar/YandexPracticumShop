package ru.yandex.practicum.tarasov.yandexpracticumshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Order;
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
        model.addAttribute("items", order.items());
        model.addAttribute("total", order.totalSum());
        model.addAttribute("empty", order.getGoods().isEmpty());
        return "cart";
    }

    @RequestMapping("/orders")
    public String getOrders(Model model) {
        List<Order> orders = orderService.getOrders();
        model.addAttribute("orders", orders);
        return "orders";
    }

    @PostMapping("/buy")
    public String buyItems(RedirectAttributes redirectAttrs) {
        long orderId = orderService.buyCart();
        redirectAttrs.addAttribute("id", orderId);
        return "redirect:/orders/{id}?newOrder=true";
    }

    @RequestMapping("/orders/{id}")
    public String getOrder(Model model,
                           @PathVariable("id") int orderId) {
        Order order = orderService.getOrder(orderId);
        model.addAttribute("order", order);
        return "order";
    }
}
