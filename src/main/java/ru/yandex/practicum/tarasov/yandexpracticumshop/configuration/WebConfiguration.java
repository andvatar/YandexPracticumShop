package ru.yandex.practicum.tarasov.yandexpracticumshop.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;


@Configuration
@PropertySource("classpath:application.properties")
public class WebConfiguration implements WebFluxConfigurer {

    @Value("${spring.images.location}")
    private String imagesLocation;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**").addResourceLocations("file:" + imagesLocation);
    }
}
