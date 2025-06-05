package ru.yandex.practicum.tarasov.yandexpracticumshop.integration;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.yandex.practicum.tarasov.yandexpracticumshop.YandexPracticumShopApplication;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.GoodsService;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.mock;

@SpringBootTest(classes = YandexPracticumShopApplication.class)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cache.type=none")
public class ImportTest {

    @Autowired
    private GoodsService goodsService;

    @Test
    public void importTest() {
        String csv = """
                title,description,img_path,quantity,price_amount
                ImportOne,ImportOne,4.png,100,19000.0
                ImportTwo,ImportTwo,3.png,50,100000.0""";

        FilePart mockFilePart = mock(FilePart.class);

        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = factory.wrap(csv.getBytes(StandardCharsets.UTF_8));
        Mockito.when(mockFilePart.content()).thenReturn(Flux.just(dataBuffer));

        StepVerifier
                .create(goodsService.importGoods(mockFilePart))
                .verifyComplete();

        StepVerifier
                .create(goodsService.findAll("",  PageRequest.of(0, 100, Sort.unsorted())))
                .expectNextCount(3)
                .verifyComplete();

        // nothing should be changed if we import the same file second time
        StepVerifier
                .create(goodsService.importGoods(mockFilePart))
                .verifyComplete();

        StepVerifier
                .create(goodsService.findAll("",  PageRequest.of(0, 100, Sort.unsorted())))
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void importEmptyFileTest() {
        String csv = "title,description,img_path,quantity,price_amount";

        FilePart mockFilePart = mock(FilePart.class);

        DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
        DataBuffer dataBuffer = factory.wrap(csv.getBytes(StandardCharsets.UTF_8));
        Mockito.when(mockFilePart.content()).thenReturn(Flux.just(dataBuffer));

        StepVerifier
                .create(goodsService.importGoods(mockFilePart))
                .verifyComplete();

        StepVerifier
                .create(goodsService.findAll("",  PageRequest.of(0, 100, Sort.unsorted())))
                .expectNextCount(1)
                .verifyComplete();
    }
}
