package com.dwarfeng.springdatamark.api.integration.springtelqos;

/**
 * 最大交互式重试次数超出异常。
 *
 * @author DwArFeng
 * @since 1.1.0
 */
public class MaxInteractiveRetryTimesExceededException extends Exception {

    private static final long serialVersionUID = -3398029849068756385L;

    private final int retryTimes;

    public MaxInteractiveRetryTimesExceededException(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    @Override
    public String getMessage() {
        return "交互式重试次数超过 " + retryTimes + " 次";
    }
}
