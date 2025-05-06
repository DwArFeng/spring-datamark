package com.dwarfeng.springdatamark.stack.exception;

/**
 * 数据标识异常。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class DatamarkException extends Exception {

    private static final long serialVersionUID = -8634988821711657693L;

    public DatamarkException() {
    }

    public DatamarkException(String message) {
        super(message);
    }

    public DatamarkException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatamarkException(Throwable cause) {
        super(cause);
    }
}
