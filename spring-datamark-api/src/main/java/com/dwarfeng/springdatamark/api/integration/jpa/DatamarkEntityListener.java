package com.dwarfeng.springdatamark.api.integration.jpa;

import com.dwarfeng.springdatamark.stack.service.DatamarkService;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

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

    private final DatamarkService datamarkService;

    private final Map<Class<?>, EntityFieldInfo> entityFieldInfoMap = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public DatamarkEntityListener(DatamarkService datamarkService) {
        this.datamarkService = datamarkService;
    }

    @SuppressWarnings("DuplicatedCode")
    @PrePersist
    public void prePersist(Object entity) throws Exception {
        lock.readLock().lock();
        try {
            if (entityFieldInfoMap.containsKey(entity.getClass())) {
                updateDatamarkField(entity, entityFieldInfoMap.get(entity.getClass()).getDatamarkFieldNames());
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            if (entityFieldInfoMap.containsKey(entity.getClass())) {
                updateDatamarkField(entity, entityFieldInfoMap.get(entity.getClass()).getDatamarkFieldNames());
            }
            EntityFieldInfo entityFieldInfo = parseEntityFieldInfo(entity);
            entityFieldInfoMap.put(entity.getClass(), entityFieldInfo);
            updateDatamarkField(entity, entityFieldInfo.getDatamarkFieldNames());
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
                updateDatamarkField(entity, entityFieldInfoMap.get(entity.getClass()).getDatamarkFieldNames());
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            if (entityFieldInfoMap.containsKey(entity.getClass())) {
                updateDatamarkField(entity, entityFieldInfoMap.get(entity.getClass()).getDatamarkFieldNames());
            }
            EntityFieldInfo entityFieldInfo = parseEntityFieldInfo(entity);
            entityFieldInfoMap.put(entity.getClass(), entityFieldInfo);
            updateDatamarkField(entity, entityFieldInfo.getDatamarkFieldNames());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void updateDatamarkField(Object entity, List<String> datamarkFieldNames) throws Exception {
        String datamark = datamarkService.get();
        PropertyUtilsBean propertyUtils = BeanUtilsBean.getInstance().getPropertyUtils();
        for (String datamarkFieldName : datamarkFieldNames) {
            propertyUtils.setProperty(entity, datamarkFieldName, datamark);
        }
    }

    private EntityFieldInfo parseEntityFieldInfo(Object entity) {
        Field[] fields = entity.getClass().getDeclaredFields();
        // 遍历 fields 寻找含有 DatamarkField 注解的字段，将所有找到的字段存储在 datamarkFieldNames 中。
        final List<String> datamarkFieldNames = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(DatamarkField.class)) {
                datamarkFieldNames.add(field.getName());
            }
        }
        // 构造结果并返回。
        return new EntityFieldInfo(datamarkFieldNames);
    }

    private static final class EntityFieldInfo {

        private final List<String> datamarkFieldNames;

        public EntityFieldInfo(List<String> datamarkFieldNames) {
            this.datamarkFieldNames = datamarkFieldNames;
        }

        public List<String> getDatamarkFieldNames() {
            return datamarkFieldNames;
        }

        @Override
        public String toString() {
            return "EntityFieldInfo{" +
                    "datamarkFieldNames=" + datamarkFieldNames +
                    '}';
        }
    }
}
