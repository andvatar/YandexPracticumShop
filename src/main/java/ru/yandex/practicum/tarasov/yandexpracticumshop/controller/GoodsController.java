package ru.yandex.practicum.tarasov.yandexpracticumshop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.GoodsService;

import java.io.IOException;
import java.util.Optional;

@Controller
public class GoodsController {
    private final GoodsService goodsService;

    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @RequestMapping
    public Mono<String> startPage() {
        return Mono.just("redirect:/main/items");
    }

    @RequestMapping("/main/items")
    public Mono<String> getAllGoods(Model model,
                              @RequestParam("pageNumber") Optional<Integer> page,
                              @RequestParam("pageSize") Optional<Integer> size,
                              @RequestParam("sort") Optional<String> sort,
                              @RequestParam(value = "search", required = false) String search) {
        return goodsService.findAll(search, page.orElse(0), size.orElse(10), sort.orElse("no"), "ASC")
                        .flatMap(goods -> {
                            model.addAttribute("paging", goods);
                            return Mono.just("main");
                        }
        );
    }

    @PostMapping("/main/items/{id}")
    public Mono<String> addRemoveGoods(@PathVariable("id") int goodsId,
                                 @RequestParam("action") String action) {
        return goodsService.addRemoveToCart(goodsId, action).thenReturn("redirect:/main/items");
        //return "redirect:/main/items";
    }

    @PostMapping("/cart/items/{id}")
    public Mono<String> addRemoveGoodsInCart( @PathVariable("id") int goodsId,
                                        @RequestParam("action") String action) {
        return goodsService.addRemoveToCart(goodsId, action).thenReturn("redirect:/cart/items");
        //return "redirect:/cart/items";
    }

    @RequestMapping("/items/{id}")
    public Mono<String> getGoods( Model model,
                            @PathVariable("id") long id) {
        return goodsService.findById(id).flatMap(goods -> {
            model.addAttribute("item", goods);
            return Mono.just("item");
        });
    }

    @RequestMapping("/import")
    public Mono<String> importPage() {
        return Mono.just("import");
    }

    @PostMapping("/import")
    public Mono<String> importGoods(@RequestParam("file") MultipartFile file) throws IOException {
        return goodsService.importGoods(file).thenReturn("redirect:/main/items");
        //return "redirect:/main/items";
    }
}
