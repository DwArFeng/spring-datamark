package com.dwarfeng.springdatamark.sdk.util;

/**
 * 约束类。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public final class Constraints {

    /**
     * 数据标识的长度约束。
     */
    public static final int LENGTH_DATAMARK = 100;

    private Constraints() {
        throw new IllegalStateException("禁止实例化");
    }
}
