package com.dwarfeng.springdatamark.impl.service;

import com.dwarfeng.springdatamark.sdk.util.DatamarkUtil;
import com.dwarfeng.springdatamark.stack.bean.DatamarkConfig;
import com.dwarfeng.springdatamark.stack.exception.*;
import com.dwarfeng.springdatamark.stack.service.DatamarkService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DatamarkServiceImpl implements DatamarkService, InitializingBean, ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatamarkServiceImpl.class);

    private DatamarkConfig datamarkConfig;
    private ApplicationContext applicationContext;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private String cachedDatamark;

    public DatamarkServiceImpl() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        lock.writeLock().lock();
        try {
            readAndUpdateCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean updateAllowed() {
        return datamarkConfig.isServiceUpdateAllowed();
    }

    @Override
    public String get() throws DatamarkException {
        lock.readLock().lock();
        try {
            if (isCached()) {
                return cachedDatamark;
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            if (isCached()) {
                return cachedDatamark;
            }
            readAndUpdateCache();
            return cachedDatamark;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void refresh() throws DatamarkException {
        lock.writeLock().lock();
        try {
            readAndUpdateCache();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String refreshAndGet() throws DatamarkException {
        lock.writeLock().lock();
        try {
            readAndUpdateCache();
            return cachedDatamark;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void update(String datamark) throws DatamarkException {
        lock.writeLock().lock();
        try {
            writeAndUpdateCache(datamark);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public DatamarkConfig getDatamarkConfig() {
        lock.readLock().lock();
        try {
            return datamarkConfig;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setDatamarkConfig(DatamarkConfig datamarkConfig) {
        lock.writeLock().lock();
        try {
            this.datamarkConfig = datamarkConfig;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ApplicationContext getApplicationContext() {
        lock.readLock().lock();
        try {
            return applicationContext;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) {
        lock.writeLock().lock();
        try {
            this.applicationContext = applicationContext;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean isCached() {
        return Objects.nonNull(cachedDatamark);
    }

    // 为了代码的可阅读性，此处不做简化。
    @SuppressWarnings("ConstantValue")
    private void readAndUpdateCache() throws DatamarkException {
        LOGGER.debug("刷新并更新缓存...");
        String tempDatamark;
        LOGGER.debug("读取资源中的内容...");
        Resource resource = applicationContext.getResource(datamarkConfig.getResourceUrl());
        try (
                InputStream in = resource.getInputStream();
                Scanner scanner = new Scanner(in, datamarkConfig.getResourceCharset())
        ) {
            if (!scanner.hasNextLine()) {
                LOGGER.debug("资源中没有下一行内容, 将 tempDatamark 设置为空字符串...");
                tempDatamark = StringUtils.EMPTY;
            } else {
                LOGGER.debug("资源中有下一行内容, 将 tempDatamark 设置为下一行的 trimmed 内容...");
                tempDatamark = StringUtils.trim(scanner.nextLine());
            }
        } catch (Exception e) {
            LOGGER.warn("刷新数据标识时发生异常, 将清除缓存并抛出异常, 异常信息如下: ", e);
            cachedDatamark = null;
            LOGGER.debug("最新缓存内容为: {}", cachedDatamark);
            throw new ResourceReadFailedException(e, datamarkConfig.getResourceUrl());
        }
        LOGGER.debug("校验 tempDatamark 内容...");
        if (!DatamarkUtil.isDatamarkValid(tempDatamark)) {
            LOGGER.warn("数据标识不合法, 将清除缓存并抛出异常");
            cachedDatamark = null;
            LOGGER.debug("最新缓存内容为: {}", cachedDatamark);
            throw new IllegalDatamarkException(tempDatamark);
        }
        LOGGER.debug("更新缓存内容为 tempDatamark...");
        cachedDatamark = tempDatamark;
        LOGGER.debug("最新缓存内容为: {}", cachedDatamark);
    }

    private void writeAndUpdateCache(String datamark) throws DatamarkException {
        LOGGER.debug("写入并更新缓存...");
        LOGGER.debug("确认服务允许更新...");
        if (!datamarkConfig.isServiceUpdateAllowed()) {
            LOGGER.debug("最新缓存内容为: {}", cachedDatamark);
            throw new UpdateNotAllowedException();
        }
        LOGGER.debug("Trim datamark, 并校验 datamark 内容...");
        datamark = StringUtils.trim(datamark);
        if (!DatamarkUtil.isDatamarkValid(datamark)) {
            LOGGER.warn("数据标识不合法, 将抛出异常");
            LOGGER.debug("最新缓存内容为: {}", cachedDatamark);
            throw new IllegalDatamarkException(datamark);
        }
        LOGGER.debug("验证资源是否可写...");
        Resource resource = applicationContext.getResource(datamarkConfig.getResourceUrl());
        if (!(resource instanceof WritableResource)) {
            LOGGER.warn("资源不可写, 将抛出异常");
            LOGGER.debug("最新缓存内容为: {}", cachedDatamark);
            throw new ResourceNotWritableException(datamarkConfig.getResourceUrl());
        }
        LOGGER.debug("向资源中写入内容...");
        try (
                OutputStream out = ((WritableResource) resource).getOutputStream();
                PrintStream ps = new PrintStream(out, false, datamarkConfig.getResourceCharset())
        ) {
            ps.println(datamark);
        } catch (Exception e) {
            LOGGER.warn("写入数据标识时发生异常, 将抛出异常, 异常信息如下: ", e);
            LOGGER.debug("最新缓存内容为: {}", cachedDatamark);
            throw new ResourceWriteFailedException(e, datamarkConfig.getResourceUrl());
        }
        LOGGER.debug("更新缓存内容为 datamark...");
        cachedDatamark = datamark;
        LOGGER.debug("最新缓存内容为: {}", cachedDatamark);
    }

    @Override
    public String toString() {
        return "DatamarkServiceImpl{" +
                "datamarkConfig=" + datamarkConfig +
                ", applicationContext=" + applicationContext +
                ", cachedDatamark='" + cachedDatamark + '\'' +
                '}';
    }
}
