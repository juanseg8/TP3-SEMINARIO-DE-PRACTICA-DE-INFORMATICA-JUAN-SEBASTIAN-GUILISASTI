package com.stockflow.exception;

public class TransferenciaException extends StockFlowException {
    public TransferenciaException(String message) { super(message); }
    public TransferenciaException(String message, Throwable cause) { super(message, cause); }
}
