package ru.yandex.practicum.tarasov.yandexpracticumshop.controller;

import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.GoodsService;

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
                        .map(goods -> {
                            model.addAttribute("paging", goods);
                            return "main";
                        }
        );
    }

    @PostMapping(value ="/main/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> addRemoveGoods(@PathVariable("id") int goodsId,
                                    ServerWebExchange serverWebExchange) {
        return serverWebExchange.getFormData()
                .flatMap(data ->
                    goodsService.addRemoveToCart(goodsId, data.toSingleValueMap().get("action")))
                .thenReturn("redirect:/main/items");
    }

    @PostMapping(value ="/cart/items/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<String> addRemoveGoodsInCart( @PathVariable("id") int goodsId,
                                              ServerWebExchange serverWebExchange) {
        return serverWebExchange.getFormData()
                .flatMap(data ->
                        goodsService.addRemoveToCart(goodsId, data.toSingleValueMap().get("action")))
                .thenReturn("redirect:/cart/items");
    }

    @RequestMapping("/items/{id}")
    public Mono<String> getGoods( Model model,
                            @PathVariable("id") long id) {
        return goodsService.findDTOById(id).map(goods -> {
            model.addAttribute("item", goods);
            return "item";
        });
    }

    @RequestMapping("/import")
    public Mono<String> importPage() {
        return Mono.just("import");
    }

    @PostMapping(value ="/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> importGoods(@RequestPart("file") Mono<FilePart> filePartMono) {
        return filePartMono.flatMap(goodsService::importGoods).thenReturn("redirect:/main/items");
    }
}
