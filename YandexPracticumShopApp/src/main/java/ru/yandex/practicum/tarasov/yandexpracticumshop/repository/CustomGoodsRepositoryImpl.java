package ru.yandex.practicum.tarasov.yandexpracticumshop.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.yandex.practicum.tarasov.yandexpracticumshop.DTO.ItemDTO;

import java.math.BigDecimal;

@Repository
public class CustomGoodsRepositoryImpl implements CustomGoodsRepository {

    private final DatabaseClient dbClient;

    public CustomGoodsRepositoryImpl(DatabaseClient dbClient) {
        this.dbClient = dbClient;
    }

    @Override
    public Flux<ItemDTO> findAllDTOByTitle(String search, long orderId, Pageable pageable) {
        StringBuilder sql = getSql(pageable);

        return dbClient.sql(sql.toString())
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
                .all();

    }

    private StringBuilder getSql(Pageable pageable) {
        StringBuilder sql = new StringBuilder("""
            select g.*, coalesce(og.quantity,0) as "count"
            from goods g
            left outer join order_goods og on g.id = og.goods_id and og.order_id = :order_id
            where g.quantity > 0
            and (:search = '' or g.title like :search or g.description like :search)""");

        pageable.getSort();
        if(pageable.getSort().equals(Sort.by("title"))) {
            sql.append(" order by g.title");
        }
        else if (pageable.getSort().equals(Sort.by("price"))) {
            sql.append(" order by g.price_amount");
        }

        sql.append(" limit :size offset :offset");
        return sql;
    }
}
