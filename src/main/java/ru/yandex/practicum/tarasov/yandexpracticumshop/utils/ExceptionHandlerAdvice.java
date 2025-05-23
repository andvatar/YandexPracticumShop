package ru.yandex.practicum.tarasov.yandexpracticumshop.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {

        LOGGER.error("Exception occurred: {}", ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}