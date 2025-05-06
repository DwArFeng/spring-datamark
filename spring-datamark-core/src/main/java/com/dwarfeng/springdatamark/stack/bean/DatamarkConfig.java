package com.dwarfeng.springdatamark.stack.bean;

/**
 * 数据标记配置。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class DatamarkConfig {

    private String resourceUrl;
    private String resourceCharset;
    private boolean serviceUpdateAllowed;

    public DatamarkConfig() {
    }

    public DatamarkConfig(String resourceUrl, String resourceCharset, boolean serviceUpdateAllowed) {
        this.resourceUrl = resourceUrl;
        this.resourceCharset = resourceCharset;
        this.serviceUpdateAllowed = serviceUpdateAllowed;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public String getResourceCharset() {
        return resourceCharset;
    }

    public void setResourceCharset(String resourceCharset) {
        this.resourceCharset = resourceCharset;
    }

    public boolean isServiceUpdateAllowed() {
        return serviceUpdateAllowed;
    }

    public void setServiceUpdateAllowed(boolean serviceUpdateAllowed) {
        this.serviceUpdateAllowed = serviceUpdateAllowed;
    }

    @Override
    public String toString() {
        return "DatamarkConfig{" +
                "resourceUrl='" + resourceUrl + '\'' +
                ", resourceCharset='" + resourceCharset + '\'' +
                ", serviceUpdateAllowed=" + serviceUpdateAllowed +
                '}';
    }
}
