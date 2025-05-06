package com.dwarfeng.springdatamark.node.config;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Datamark 命名空间处理器。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class SpringDatamarkNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("config", new SpringDatamarkDefinitionParser());
    }
}
