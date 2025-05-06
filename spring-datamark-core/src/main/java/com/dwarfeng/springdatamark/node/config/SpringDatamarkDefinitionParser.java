package com.dwarfeng.springdatamark.node.config;

import com.dwarfeng.springdatamark.impl.service.DatamarkServiceImpl;
import com.dwarfeng.springdatamark.stack.bean.DatamarkConfig;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * Datamark 相关的 BeanDefinitionParser。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class SpringDatamarkDefinitionParser implements BeanDefinitionParser {

    private static final String DATAMARK_NAMESPACE_URL = "http://dwarfeng.com/schema/spring-datamark";

    @Override
    public BeanDefinition parse(Element element, @Nonnull ParserContext parserContext) {
        // 获取 bean 名称。
        String configId = ParserUtil.mayResolve(parserContext, element.getAttribute("config-id"));
        String serviceId = ParserUtil.mayResolve(parserContext, element.getAttribute("service-id"));
        // 检查 bean 名称是否重复。
        checkBeanDuplicated(parserContext, configId);
        checkBeanDuplicated(parserContext, serviceId);
        // 构造 DatamarkConfig。
        BeanDefinitionBuilder datamarkConfigBuilder = BeanDefinitionBuilder.rootBeanDefinition(DatamarkConfig.class);
        // 解析 resource-setting。
        Element resourceSettingElement = (Element) element.getElementsByTagNameNS(
                DATAMARK_NAMESPACE_URL, "resource-setting").item(0);
        if (Objects.isNull(resourceSettingElement)) {
            datamarkConfigBuilder.addPropertyValue("resourceUrl", "classpath:datamark/storage");
            datamarkConfigBuilder.addPropertyValue("resourceCharset", "UTF-8");
        } else {
            datamarkConfigBuilder.addPropertyValue("resourceUrl", ParserUtil.mayResolve(
                    parserContext, resourceSettingElement.getAttribute("url")));
            datamarkConfigBuilder.addPropertyValue("resourceCharset", ParserUtil.mayResolve(
                    parserContext, resourceSettingElement.getAttribute("charset")));
        }
        // 解析 service-setting。
        Element serviceSettingElement = (Element) element.getElementsByTagNameNS(
                DATAMARK_NAMESPACE_URL, "service-setting").item(0);
        if (Objects.isNull(serviceSettingElement)) {
            datamarkConfigBuilder.addPropertyValue("serviceUpdateAllowed", true);
        } else {
            datamarkConfigBuilder.addPropertyValue("serviceUpdateAllowed", ParserUtil.mayResolve(
                    parserContext, serviceSettingElement.getAttribute("update-allowed")));
        }
        // 注册 DatamarkConfig。
        datamarkConfigBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        datamarkConfigBuilder.setLazyInit(false);
        parserContext.getRegistry().registerBeanDefinition(configId, datamarkConfigBuilder.getBeanDefinition());
        // 构造 DatamarkService。
        BeanDefinitionBuilder datamarkServiceBuilder = BeanDefinitionBuilder.rootBeanDefinition(DatamarkServiceImpl.class);
        // DatamarkService 参数赋值。
        datamarkServiceBuilder.addPropertyValue("datamarkConfig", new RuntimeBeanReference(configId));
        // 注册构造 DatamarkService。
        datamarkServiceBuilder.setScope(BeanDefinition.SCOPE_SINGLETON);
        datamarkServiceBuilder.setLazyInit(false);
        parserContext.getRegistry().registerBeanDefinition(serviceId, datamarkServiceBuilder.getBeanDefinition());

        return null;
    }

    private void checkBeanDuplicated(ParserContext parserContext, String id) {
        if (parserContext.getRegistry().containsBeanDefinition(id)) {
            throw new IllegalStateException("Duplicated spring bean id " + id);
        }
    }
}
