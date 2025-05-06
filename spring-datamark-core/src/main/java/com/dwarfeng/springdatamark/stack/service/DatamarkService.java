package com.dwarfeng.springdatamark.stack.service;

import com.dwarfeng.springdatamark.stack.exception.DatamarkException;

/**
 * 数据标识服务。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public interface DatamarkService {

    /**
     * 返回服务是否允许更新。
     *
     * @return 服务是否允许更新。
     */
    boolean updateAllowed();

    /**
     * 获取数据标识。
     *
     * @return 数据标识。
     * @throws DatamarkException 数据标识异常。
     */
    String get() throws DatamarkException;

    /**
     * 刷新数据标识。
     *
     * @throws DatamarkException 数据标识异常。
     */
    void refresh() throws DatamarkException;

    /**
     * 刷新数据标识并获取。
     *
     * @return 刷新后的数据标识。
     * @throws DatamarkException 数据标识异常。
     */
    String refreshAndGet() throws DatamarkException;

    /**
     * 更新数据标识。
     *
     * @param datamark 数据标识。
     * @throws DatamarkException 数据标识异常。
     */
    void update(String datamark) throws DatamarkException;
}
