package com.victusstore.exception;

public class StockInsufficientException extends RuntimeException {
    private Long variantId;
    private Integer availableStock;
    private Integer requestedQuantity;

    public StockInsufficientException(String message) {
        super(message);
    }

    public StockInsufficientException(String message, Long variantId, Integer availableStock, Integer requestedQuantity) {
        super(message);
        this.variantId = variantId;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    public Long getVariantId() {
        return variantId;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
}

