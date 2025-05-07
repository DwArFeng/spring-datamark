package com.dwarfeng.springdatamark.api.integration.jpa;

import java.lang.annotation.*;

/**
 * 数据标识字段。
 *
 * <p>
 * 该注解应该被添加到 JPA 实体中，与 JPA 注解一同使用。
 *
 * <p>
 * 示例代码如下：
 * <blockquote><pre>
 * {@literal @}Entity
 * {@literal @}Table(name = "tbl_user")
 * {@literal @}EntityListeners(DatamarkEntityListener.class)
 * public class HibernateUser implements Bean {
 *
 *     // 省略之前的代码...
 *
 *     // 使用 DatamarkField
 *     {@literal @}DatamarkField
 *     {@literal @}Column(
 *             name = "created_datamark",
 *             length = Constraints.LENGTH_DATAMARK,
 *             updatable = false
 *     )
 *     private String createdDatamark;
 *
 *     {@literal @}DatamarkField
 *     {@literal @}Column(
 *             name = "modified_datamark",
 *             length = Constraints.LENGTH_DATAMARK
 *     )
 *     private String modifiedDatamark;
 *
 *     // 省略之后的代码...
 * }
 * </pre></blockquote>
 *
 * @author DwArFeng
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface DatamarkField {
}
