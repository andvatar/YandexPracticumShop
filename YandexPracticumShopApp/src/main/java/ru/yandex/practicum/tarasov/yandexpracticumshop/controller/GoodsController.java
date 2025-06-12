package ru.yandex.practicum.tarasov.yandexpracticumshop.controller;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.ItemDto;
import ru.yandex.practicum.tarasov.yandexpracticumshop.enums.ErrorMessages;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.GoodsService;

import java.util.List;
import java.util.NoSuchElementException;
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

        Pageable pageable = PageRequest.of(page.orElse(0),  size.orElse(10), sort.map(Sort::by).orElseGet(Sort::unsorted));

        Mono<List<ItemDto>> items = goodsService.findAll(search,pageable).collectList();
        Mono<Integer> itemsCount = goodsService.count(search);

        return Mono.zip(items, itemsCount)
                .map(tuple2 -> {
                    model.addAttribute("paging", new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
                    return "main";
               });
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
        })
        .switchIfEmpty(Mono.error(new NoSuchElementException(ErrorMessages.ITEM_NOT_FOUND.getMessage() + id)));
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
