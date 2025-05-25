package ru.yandex.practicum.tarasov.yandexpracticumshop.service;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.domain.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.ItemDTO;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.*;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.GoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderGoodsRepository;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

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

    public Mono<Page<ItemDTO>> findAll(String search, int page, int size, String sortBy, String order) {
        Pageable pageable = PageRequest.of(page, size, Sort.unsorted());
        return orderService.getCartDTO()
                .flatMap(cart -> goodsRepository.findAllDTOByTitle(search, cart.id(), pageable, sortBy, order));
    }

    public Mono<Goods> findById(long id) {
        return goodsRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("No goods found with id: " + id)));
    }

    public Mono<ItemDTO> findDTOById(long id) {
        return orderService.getCartDTO()
                .flatMap(cart ->
                    goodsRepository.findDTOById(id, cart.id())
                    .switchIfEmpty(Mono.error(new NoSuchElementException("No goods found with id: " + id))));
    }

    @Transactional
    public Mono<Void> addRemoveToCart(long goodsId, String action) {
        System.out.println("addRemoveToCart" + goodsId + " " + action);

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
                                    return Mono.error(new NoSuchElementException("No goods found with id: " + goodsId));
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
                            );
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
        );
    }

    private Mono<Void> addRemoveGoods(OrderGoods orderGoods, int amount, int goodsQuantity) {
        int quantityInCart = orderGoods.getQuantity();
        if(quantityInCart + amount > goodsQuantity) {
            throw new NoSuchElementException("Not enough goods in store");
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
