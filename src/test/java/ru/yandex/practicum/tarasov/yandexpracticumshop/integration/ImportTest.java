package ru.yandex.practicum.tarasov.yandexpracticumshop.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.tarasov.yandexpracticumshop.YandexPracticumShopApplication;
import ru.yandex.practicum.tarasov.yandexpracticumshop.service.GoodsService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = YandexPracticumShopApplication.class)
@ActiveProfiles("test")
public class ImportTest {

    @Autowired
    private GoodsService goodsService;

    @Test
    public void importTest() throws IOException {
        String csv = """
                title,description,img_path,quantity,price_amount
                ImportOne,ImportOne,4.png,100,19000
                ImportTwo,ImportTwo,3.png,50,100000""";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        goodsService.importGoods(mockMultipartFile);
        assertEquals(3, goodsService.findAll("", 0, 100, "no", "asc").getContent().size());

        // nothing should be changed if we import the same file second time
        goodsService.importGoods(mockMultipartFile);
        assertEquals(3, goodsService.findAll("", 0, 100, "no", "asc").getContent().size());
    }

    @Test
    public void importEmptyFileTest() throws IOException {
        String csv = "title,description,img_path,quantity,price_amount";
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "test.csv", "text/csv", csv.getBytes());

        goodsService.importGoods(mockMultipartFile);
        assertEquals(1, goodsService.findAll("", 0, 100, "no", "asc").getContent().size());
    }
}
