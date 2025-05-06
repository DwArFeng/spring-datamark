package com.dwarfeng.springdatamark.sdk.util;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 数据标识工具类。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public final class DatamarkUtil {

    /**
     * 检查数据标识是否合法。
     *
     * @param datamark 数据标识。
     * @return 数据标识是否合法。
     */
    // 为了代码的可阅读性，此处不做简化。
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isDatamarkValid(@Nullable String datamark) {
        if (Objects.isNull(datamark)) {
            return false;
        }
        if (datamark.length() > Constraints.LENGTH_DATAMARK) {
            return false;
        }
        return datamark.matches("^[a-zA-Z0-9.\\-_]*$");
    }

    private DatamarkUtil() {
        throw new IllegalStateException("禁止实例化");
    }
}
