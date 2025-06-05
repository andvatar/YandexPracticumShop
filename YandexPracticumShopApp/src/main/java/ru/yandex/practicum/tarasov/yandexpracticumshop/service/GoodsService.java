package ru.yandex.practicum.tarasov.yandexpracticumshop.service;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.domain.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.ItemDTO;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.*;
import ru.yandex.practicum.tarasov.yandexpracticumshop.enums.ErrorMessages;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.GoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderGoodsRepository;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

import static ru.yandex.practicum.tarasov.yandexpracticumshop.enums.ErrorMessages.NO_GOODS;

@Service
public class GoodsService {
    private final GoodsRepository goodsRepository;
    private final OrderGoodsRepository orderGoodsRepository;
    private final OrderService orderService;
    private final CacheManager cacheManager;

    public GoodsService(GoodsRepository goodsRepository,
                        OrderGoodsRepository orderGoodsRepository,
                        OrderService orderService,
                        CacheManager cacheManager) {
        this.goodsRepository = goodsRepository;
        this.orderGoodsRepository = orderGoodsRepository;
        this.orderService = orderService;
        this.cacheManager = cacheManager;
    }

    @Cacheable(
            value = "items"
    )
    public Flux<ItemDTO> findAll(String search, Pageable pageable) {
        return orderService.getCartDTO()
                .flatMapMany(cart -> goodsRepository.findAllDTOByTitle(search, cart.id(), pageable));
    }

    @Cacheable(
            value = "items"
    )
    public Mono<Integer> count(String search) {
        return goodsRepository.countByTitle(search);
    }

    public Mono<Goods> findById(long id) {
        return goodsRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException(ErrorMessages.ITEM_NOT_FOUND.getMessage() + id)));
    }

    @Cacheable (
            value = "item",
            key = "#id"
    )
    public Mono<ItemDTO> findDTOById(long id) {
        return orderService.getCartDTO()
                .flatMap(cart ->
                    goodsRepository.findDTOById(id, cart.id())
                    .switchIfEmpty(Mono.error(new NoSuchElementException(ErrorMessages.ITEM_NOT_FOUND.getMessage() + id))));
    }

    @Transactional
    @Caching(evict = {
                @CacheEvict(value = "item", key = "#goodsId"),
                @CacheEvict(value = "items", allEntries = true)
            }
    )
    public Mono<Void> addRemoveToCart(long goodsId, String action) {

        return Mono.zip(orderService.getCart(), goodsRepository.findById(goodsId))
                .flatMap(tuple2 -> {
                    Order cart = tuple2.getT1();
                    Goods goods = tuple2.getT2();
                    int goodsQuantity = goods.getQuantity();

                    return orderGoodsRepository.findByOrderIdAndGoodsId(cart.getId(), goodsId)
                            .switchIfEmpty(Mono.defer(() -> {
                                if (action.equals("plus")) {
                                    return Mono.just(new OrderGoods(cart, goods, 0));
                                } else {
                                    return Mono.error(new NoSuchElementException(ErrorMessages.ITEM_NOT_FOUND.getMessage() + goodsId));
                                }
                            }))
                            .flatMap(
                                    orderGoods ->
                                            switch (action) {
                                                case "plus" -> addRemoveGoods(orderGoods, 1, goodsQuantity);
                                                case "minus" ->
                                                        addRemoveGoods(orderGoods, -1, goodsQuantity);
                                                case "delete" ->
                                                        orderGoodsRepository.delete(orderGoods).then();
                                                default ->
                                                        Mono.error(new NoSuchElementException("Unknown action: " + action));
                                            }
                            )
                            .doOnSuccess(v -> {
                                var cache = cacheManager.getCache("itemsByOrder");
                                if(cache != null) {
                                    cache.evict(cart.getId());
                                }
                            });
                });
    }

    @Transactional
    public Mono<Void> importGoods(FilePart file) {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        Mono<List<Object>> goodsListMono = DataBufferUtils.join(file.content())
                .map(content -> {
                    try {
                        return mapper
                                .readerFor(Goods.class)
                                .with(schema)
                                .readValues(content.asInputStream(true))
                                .readAll();
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to read CSV content: " + e.getMessage(), e);
                    }
                })
                .onErrorResume(e -> Mono.error(new RuntimeException("Error processing file content: " + e.getMessage(), e)));


        return goodsListMono.flatMap(goodsList ->
                Flux.fromIterable(goodsList)
                        .flatMap(o -> {
                            Goods goods = (Goods) o;
                            return goodsRepository.findByTitle(goods.getTitle())
                                    .switchIfEmpty(Mono.just(goods))
                                    .flatMap(existingGoods ->
                                    {
                                        existingGoods.setDescription(goods.getDescription());
                                        existingGoods.setPrice(goods.getPrice());
                                        existingGoods.setQuantity(goods.getQuantity());
                                        existingGoods.setImgPath(goods.getImgPath());
                                        return goodsRepository.save(existingGoods).then();
                                    });
                        })
                        .then()
                )
                .doOnSuccess(v -> cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear()));
    }

    private Mono<Void> addRemoveGoods(OrderGoods orderGoods, int amount, int goodsQuantity) {
        int quantityInCart = orderGoods.getQuantity();
        if(quantityInCart + amount > goodsQuantity) {
            throw new NoSuchElementException(ErrorMessages.NO_GOODS.getMessage());
        }
        if(quantityInCart + amount == 0) {
            return orderGoodsRepository.delete(orderGoods).then();
        }
        else {
            orderGoods.setQuantity(orderGoods.getQuantity() + amount);
            return orderGoodsRepository.save(orderGoods).then();
        }
    }

}
