package com.dwarfeng.springdatamark.stack.exception;

/**
 * 非法的数据标识异常。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class IllegalDatamarkException extends DatamarkException {

    private static final long serialVersionUID = 7730850300091553839L;

    private final String illegalDatamark;

    public IllegalDatamarkException(String illegalDatamark) {
        this.illegalDatamark = illegalDatamark;
    }

    public IllegalDatamarkException(Throwable cause, String illegalDatamark) {
        super(cause);
        this.illegalDatamark = illegalDatamark;
    }

    @Override
    public String getMessage() {
        return "非法的数据标识: " + illegalDatamark;
    }
}
