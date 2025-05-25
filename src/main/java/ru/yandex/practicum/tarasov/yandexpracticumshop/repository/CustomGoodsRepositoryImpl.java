package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.ItemDTO;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class CustomGoodsRepositoryImpl implements CustomGoodsRepository {

    private final DatabaseClient dbClient;

    public CustomGoodsRepositoryImpl(DatabaseClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public Mono<Page<ItemDTO>> findAllDTOByTitle(String search, long orderId, Pageable pageable, String sortBy, String order) {
        StringBuilder sql = getSql(sortBy, order);

        String countSql = "select count(*) from goods g where g.quantity > 0 and (:search = '' or g.title like :search or g.description like :search)";

        Mono<List<ItemDTO>> items = dbClient.sql(sql.toString())
                .bind("order_id", orderId)
                .bind("search", search == null ? "" : "%" + search + "%")
                .bind("size", pageable.getPageSize())
                .bind("offset", pageable.getOffset())
                .map((row, metadata) -> new ItemDTO(
                        row.get("id", Long.class),
                        row.get("title", String.class),
                        row.get("description", String.class),
                        row.get("price_amount", BigDecimal.class),
                        row.get("count", Integer.class),
                        row.get("img_path", String.class)
                        )
                )
                .all()
                .collectList();

        Mono<Long> itemsCount = dbClient.sql(countSql)
                .bind("search", search == null ? "" : search)
                .map((row, metadata) -> (Long) row.get(0))
                .one();

        return Mono.zip(items, itemsCount)
                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
    }

    private StringBuilder getSql(String sortBy, String order) {
        StringBuilder sql = new StringBuilder("""
            select g.*, coalesce(og.quantity,0) as "count"
            from goods g
            left outer join order_goods og on g.id = og.goods_id and og.order_id = :order_id
            where g.quantity > 0
            and (:search = '' or g.title like :search or g.description like :search)""");

        if(sortBy != null && sortBy.equals("title")) {
            sql.append(" order by g.title");
            if (order != null && order.equals("desc")) {
                sql.append(" desc");
            }
        }

        if(sortBy != null && sortBy.equals("price")) {
            sql.append(" order by g.price_amount");
            if (order != null && order.equals("desc")) {
                sql.append(" desc");
            }
        }

        sql.append(" limit :size offset :offset");
        return sql;
    }
}
