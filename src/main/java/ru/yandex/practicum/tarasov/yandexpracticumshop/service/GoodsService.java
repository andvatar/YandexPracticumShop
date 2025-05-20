package ru.yandex.practicum.tarasov.yandexpracticumshop.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.entity.*;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.GoodsRepository;
import ru.yandex.practicum.tarasov.yandexpracticumshop.repository.OrderGoodsRepository;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

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

    public Mono<Page<Goods>> findAll(String search, int page, int size, String sortBy, String order) {
        Pageable pageable = PageRequest.of(page, size, Objects.equals(sortBy, "no") ? Sort.unsorted() : Sort.by(Sort.Direction.fromString(order), sortBy));
        if(search == null || search.isEmpty()) {
            return goodsRepository.findAllByQuantityGreaterThan(0, pageable)
                    .collectList()
                    .zipWith(goodsRepository.countByQuantityGreaterThan(0))
                    .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
        }
        else{
            return goodsRepository.findAllByTitleOrDescription(search, search, pageable)
                    .collectList()
                    .zipWith(goodsRepository.countByTitleOrDescription(search, search))
                    .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2()));
        }
    }

    public Mono<Goods> findById(long id) {
        return goodsRepository.findById(id)
                .switchIfEmpty(Mono.error(new NoSuchElementException("No goods found with id: " + id)));
    }

    @Transactional
    public Mono<Void> addRemoveToCart(long goodsId, String action) {

        return orderService.getCart()
                .flatMap(cart -> {
                    long orderId = cart.getId();
                    return findById(goodsId)
                            .flatMap(goods -> {
                                        int goodsQuantity = goods.getQuantity();

                                        return orderGoodsRepository.findByIdOrderIdAndIdGoodsId(orderId, goodsId)
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
                                    }
                            );
                });
    }

    @Transactional
    public Mono<Void> importGoods(MultipartFile file) throws IOException {
        CsvMapper mapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        MappingIterator<Goods> goodsIt = mapper
                .readerFor(Goods.class)
                .with(schema)
                .readValues(file.getInputStream());

        return Flux.fromIterable(goodsIt.readAll())
                        .flatMap(goods ->
                                    goodsRepository.findByTitle(goods.getTitle())
                                            .switchIfEmpty(Mono.just(goods))
                                            .flatMap(existingGoods ->
                                            {
                                                existingGoods.setDescription(goods.getDescription());
                                                existingGoods.setPrice(goods.getPrice());
                                                existingGoods.setQuantity(goods.getQuantity());
                                                existingGoods.setImgPath(goods.getImgPath());
                                                return goodsRepository.save(existingGoods).then();
                                            }).then()

                        )
                .then();
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
