package com.stockflow.exception;

public class StockFlowException extends RuntimeException {
    public StockFlowException(String message) { super(message); }
    public StockFlowException(String message, Throwable cause) { super(message, cause); }
}
