package ru.yandex.practicum.tarasov.yandexpracticumshop.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.Goods;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.GoodsService;

import java.util.Optional;

@Controller
public class GoodsController {
    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @RequestMapping
    public String startPage() {
        return "redirect:/main/items";
    }

    @RequestMapping("/main/items")
    public String getAllGoods(Model model,
                              @RequestParam("pageNumber") Optional<Integer> page,
                              @RequestParam("pageSize") Optional<Integer> size,
                              @RequestParam("sort") Optional<String> sort) {
        Page<Goods> goods = goodsService.findAll(page.orElse(0), size.orElse(10), sort.orElse("no"), "ASC");
        model.addAttribute("paging", goods);
        return "main";
    }

    @PostMapping("/main/items/{id}")
    public String addRemoveGoods(@PathVariable("id") int goodsId,
                                 @RequestParam("action") String action) {
        goodsService.addRemoveToCart(goodsId, action);
        return "redirect:/main/items";
    }

    @PostMapping("/cart/items/{id}")
    public String addRemoveGoodsInCart( @PathVariable("id") int goodsId,
                                        @RequestParam("action") String action) {
        goodsService.addRemoveToCart(goodsId, action);
        return "redirect:/cart/items";
    }
}
