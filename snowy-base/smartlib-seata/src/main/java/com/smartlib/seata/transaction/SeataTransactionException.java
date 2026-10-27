package com.smartlib.seata.transaction;

/** 基础事务异常，由业务调用方按自己的错误协议转换。 */
public class SeataTransactionException extends RuntimeException {
    public SeataTransactionException(String message) {
        super(message);
    }

    public SeataTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
