package com.dwarfeng.springdatamark.stack.exception;

/**
 * 资源读取失败异常。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class ResourceReadFailedException extends DatamarkException {

    private static final long serialVersionUID = 3368084611093241320L;

    private final String resourceUrl;

    public ResourceReadFailedException(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public ResourceReadFailedException(Throwable cause, String resourceUrl) {
        super(cause);
        this.resourceUrl = resourceUrl;
    }

    @Override
    public String getMessage() {
        return "资源读取失败: " + resourceUrl;
    }
}
