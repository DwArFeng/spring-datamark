package com.dwarfeng.springdatamark.example;

import com.dwarfeng.springdatamark.stack.service.DatamarkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Scanner;

/**
 * 流程示例。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
public class ProcessExample {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExample.class);

    public static void main(String[] args) {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                "classpath:spring/application-context*.xml"
        );
        ctx.registerShutdownHook();
        ctx.start();

        DatamarkService datamarkService = ctx.getBean(DatamarkService.class);

        Scanner scanner = new Scanner(System.in);

        // 显示欢迎信息。
        System.out.println("开发者您好!");
        System.out.println("这是一个示例, 用于演示 spring-datamark 的功能");
        System.out.println("spring-datamark 是一个数据标记服务, 用于为数据提供当前时刻的标记");
        System.out.println("为了更好的进行体验, 请您在运行本示例之前, 按照文档的说明创建配置文件");
        System.out.print("请按回车键继续...");
        scanner.nextLine();

        // 1. 获取当前的数据标记。
        String currentDatamark = null;
        System.out.println();
        System.out.println("1. 获取当前的数据标记...");
        try {
            currentDatamark = datamarkService.get();
        } catch (Exception e) {
            LOGGER.warn("获取当前的数据标记失败, 异常信息如下: ", e);
        }
        System.out.println("当前的数据标记为: " + currentDatamark);
        System.out.print("请按回车键继续...");
        scanner.nextLine();

        // 2. 刷新数据标记。
        System.out.println();
        System.out.println("2. 刷新数据标记...");
        System.out.println("请编辑 ${datamark.resource.url} 对应的资源中的内容, 将其更改为新的数据标记");
        System.out.print("请按回车键继续...");
        scanner.nextLine();
        try {
            currentDatamark = datamarkService.refreshAndGet();
        } catch (Exception e) {
            LOGGER.warn("刷新并获取当前的数据标记失败, 异常信息如下: ", e);
        }
        System.out.println("当前的数据标记为: " + currentDatamark);
        System.out.println("请观察刷新后的数据标记是否与您编辑后的数据标记一致");
        System.out.print("请按回车键继续...");
        scanner.nextLine();

        // 3. 更新数据标记。
        System.out.println();
        System.out.println("3. 更新数据标记...");
        System.out.println("请指定一个新的数据标记...");
        String neoDatamark = scanner.nextLine();
        try {
            datamarkService.update(neoDatamark);
        } catch (Exception e) {
            LOGGER.warn("更新数据标记失败, 异常信息如下: ", e);
        }
        System.out.println(
                "数据标记更新完毕，请查看 ${datamark.resource.url} 对应的资源中的内容，观察是否与您指定的新的数据标记一致"
        );
        System.out.print("请按回车键继续...");
        scanner.nextLine();
        try {
            currentDatamark = datamarkService.get();
        } catch (Exception e) {
            LOGGER.warn("获取当前的数据标记失败, 异常信息如下: ", e);
        }
        System.out.println("当前的数据标记为: " + currentDatamark);
        System.out.println("请观察当前的数据标记是否与您指定的新的数据标记一致");
        System.out.print("请按回车键继续...");
        scanner.nextLine();

        // 显示结束信息。
        System.out.println();
        System.out.println("示例演示完毕, 感谢您测试与使用!");

        ctx.stop();
        ctx.close();
        System.exit(0);
    }
}
