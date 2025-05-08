package com.dwarfeng.springdatamark.api.integration.jpa;

import com.dwarfeng.springdatamark.stack.service.DatamarkService;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 数据标识实体侦听器。
 *
 * <p>
 * 侦听器在工作时，会寻找参数对象对应的类的所有包含 {@link DatamarkField} 注解的所有声明字段（包括私有字段，但不包括父类字段），
 * 并将这些字段的值通过 Bean 方法设置为当前的数据标识。
 *
 * @author DwArFeng
 * @see DatamarkField
 * @since 1.0.0
 */
public class DatamarkEntityListener {

    private final Map<String, DatamarkService> datamarkServiceMap;

    private final Map<Class<?>, EntityInfo> entityFieldInfoMap = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public DatamarkEntityListener(Map<String, DatamarkService> datamarkServiceMap) {
        this.datamarkServiceMap = datamarkServiceMap;
    }

    @SuppressWarnings("DuplicatedCode")
    @PrePersist
    public void prePersist(Object entity) throws Exception {
        lock.readLock().lock();
        try {
            if (entityFieldInfoMap.containsKey(entity.getClass())) {
                updateDatamarkField(entity, entityFieldInfoMap.get(entity.getClass()));
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            if (entityFieldInfoMap.containsKey(entity.getClass())) {
                updateDatamarkField(entity, entityFieldInfoMap.get(entity.getClass()));
            }
            EntityInfo entityInfo = parseEntityInfo(entity);
            entityFieldInfoMap.put(entity.getClass(), entityInfo);
            updateDatamarkField(entity, entityInfo);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @SuppressWarnings("DuplicatedCode")
    @PreUpdate
    public void preUpdate(Object entity) throws Exception {
        lock.readLock().lock();
        try {
            if (entityFieldInfoMap.containsKey(entity.getClass())) {
                updateDatamarkField(entity, entityFieldInfoMap.get(entity.getClass()));
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            if (entityFieldInfoMap.containsKey(entity.getClass())) {
                updateDatamarkField(entity, entityFieldInfoMap.get(entity.getClass()));
            }
            EntityInfo entityInfo = parseEntityInfo(entity);
            entityFieldInfoMap.put(entity.getClass(), entityInfo);
            updateDatamarkField(entity, entityInfo);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void updateDatamarkField(Object entity, EntityInfo entityInfo) throws Exception {
        for (EntityFieldInfo fieldInfo : entityInfo.getFieldInfos()) {
            BeanUtilsBean.getInstance().getPropertyUtils().setProperty(
                    entity,
                    fieldInfo.getFieldName(),
                    fieldInfo.getDatamarkService().get()
            );
        }
    }

    private EntityInfo parseEntityInfo(Object entity) {
        // 如果 datamarkServiceMap 为空映射，直接抛出异常。
        if (datamarkServiceMap.isEmpty()) {
            throw new IllegalStateException("应用上下文中不存在任何 DatamarkService");
        }

        Field[] fields = entity.getClass().getDeclaredFields();
        // 遍历 fields 寻找含有 DatamarkField 注解的字段，解析 entityFieldInfo，并添加到 entityFieldInfos 中。
        final List<EntityFieldInfo> entityFieldInfos = new ArrayList<>();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(DatamarkField.class)) {
                continue;
            }
            entityFieldInfos.add(parseEntityFieldInfo(entity, field));
        }
        // 构造结果并返回。
        return new EntityInfo(entityFieldInfos);
    }

    private EntityFieldInfo parseEntityFieldInfo(Object entity, Field field) {
        DatamarkField datamarkField = field.getAnnotation(DatamarkField.class);
        // 在方法调用的时候，已经保证了 datamarkField 不会是 null。
        assert datamarkField != null;
        String serviceId = datamarkField.serviceId();
        // 解析 datamarkService。
        DatamarkService datamarkService;
        /*
         * 当 serviceId 是空字符串时：
         * 1. 如果只有一个 datamarkService，那么选用这个 datamarkService。
         * 2. 如果有多个 dataMarkService，抛出异常。
         */
        if (StringUtils.isEmpty(serviceId)) {
            if (datamarkServiceMap.size() == 1) {
                datamarkService = datamarkServiceMap.values().stream().findAny().get();
            } else {
                String message = entity.getClass().getCanonicalName() + "." + field.getName() +
                        " 字段中 @DatamarkField 注解的 serviceId 未指定（或为空字符串）, " +
                        "但应用上下文中存在多个 DatamarkService";
                throw new IllegalStateException(message);
            }
        }
        /*
         * 当 serviceId 不是空字符串时：
         * 1. 取 serviceId 对应的 datamarkService。
         * 2. 如果 serviceId 对应的 datamarkService 不存在，则抛出异常。
         */
        else {
            if (datamarkServiceMap.containsKey(serviceId)) {
                datamarkService = datamarkServiceMap.get(serviceId);
            } else {
                String message = entity.getClass().getCanonicalName() + "." + field.getName() +
                        " 字段中 @DatamarkField 注解的 serviceId 为 " + serviceId +
                        ", 但应用上下文中不存在对应的 DatamarkService";
                throw new IllegalStateException(message);
            }
        }
        // 解析 fieldName。
        String fieldName = field.getName();
        // 构造结果并返回。
        return new EntityFieldInfo(datamarkService, fieldName);
    }

    private static final class EntityInfo {

        private final List<EntityFieldInfo> fieldInfos;

        public EntityInfo(@Nonnull List<EntityFieldInfo> fieldInfos) {
            this.fieldInfos = fieldInfos;
        }

        @Nonnull
        public List<EntityFieldInfo> getFieldInfos() {
            return fieldInfos;
        }

        @Override
        public String toString() {
            return "EntityInfo{" +
                    "fieldInfos=" + fieldInfos +
                    '}';
        }
    }

    private static final class EntityFieldInfo {

        private final DatamarkService datamarkService;
        private final String fieldName;

        public EntityFieldInfo(
                @Nonnull DatamarkService datamarkService,
                @Nonnull String fieldName
        ) {
            this.datamarkService = datamarkService;
            this.fieldName = fieldName;
        }

        public DatamarkService getDatamarkService() {
            return datamarkService;
        }

        public String getFieldName() {
            return fieldName;
        }

        @Override
        public String toString() {
            return "EntityFieldInfo{" +
                    "datamarkService=" + datamarkService +
                    ", fieldName='" + fieldName + '\'' +
                    '}';
        }
    }
}
