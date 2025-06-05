package ru.yandex.practicum.tarasov.yandexpracticumshop.enums;

public enum ErrorMessages {
    NO_GOODS("Not enough goods in the store"),
    ITEM_NOT_FOUND("No goods found with id: "),
    ORDER_NOT_FOUND("No order found with id: "),
    EMPTY_CART("The cart is empty"),
    NO_GOODS_WITH_TITLE("Not enough goods in the store: ");

    private final String message;

    ErrorMessages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
