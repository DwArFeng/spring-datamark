# spring-datamark

一款基于 Spring 框架的数据标记处理器。

## 特性

1. 能够轻松地通过配置获取一个数据标记处理器，以获取当前的数据标记的值。
2. 数据标记基于 Spring Resource 进行加载。
3. 提供标记刷新 API，可以重复读取 Spring Resource，并刷新数据标记。
4. 提供标记更新 API，当 Spring Resource 支持写入时，可以更新数据标记。
5. 使用读写锁，线程安全的同时提高并发效率。

运行 `spring-datamark-core` 模块中 `src/test` 下的示例以观察全部特性。

| 示例类名                                               | 说明   |
|----------------------------------------------------|------|
| com.dwarfeng.springdatamark.example.ProcessExample | 流程示例 |

## 文档

该项目的文档位于 [docs](../../../docs) 目录下，包括：

### wiki

wiki 为项目的开发人员为本项目编写的详细文档，包含不同语言的版本，主要入口为：

1. [简介](./Introduction.md) - 即本文件。
2. [目录](./Contents.md) - 文档目录。

## 如何使用

1. 运行 `spring-datamark-core` 模块中 `src/test` 下的示例以观察全部特性。
2. 在项目中添加 `xml` 配置文件，并确保此配置文件能够被 spring 加载。

`xml` 配置文件示例如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:datamark="http://dwarfeng.com/schema/spring-datamark"
        xmlns="http://www.springframework.org/schema/beans"
        xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://dwarfeng.com/schema/spring-datamark
        http://dwarfeng.com/schema/spring-datamark/spring-datamark.xsd"
>

    <!-- datamark:config 中所有配置属性均支持 SPEL 表达式。 -->
    <datamark:config>
        <datamark:resource-setting url="${datamark.resource.url}" charset="#{T(System).getProperty('file.encoding')}"/>
        <datamark:service-setting update-allowed="false"/>
    </datamark:config>
</beans>
```
