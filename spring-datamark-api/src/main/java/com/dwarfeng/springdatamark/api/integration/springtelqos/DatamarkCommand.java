package com.dwarfeng.springdatamark.api.integration.springtelqos;

import com.dwarfeng.springdatamark.sdk.util.DatamarkUtil;
import com.dwarfeng.springdatamark.stack.service.DatamarkService;
import com.dwarfeng.springtelqos.node.config.TelqosCommand;
import com.dwarfeng.springtelqos.sdk.command.CliCommand;
import com.dwarfeng.springtelqos.stack.command.Context;
import com.dwarfeng.springtelqos.stack.exception.TelqosException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据标识指令。
 *
 * <p>
 * 用于提供 <code>spring-telqos</code> 框架的指令注册。
 *
 * @author DwArFeng
 * @since 1.0.0
 */
@TelqosCommand
public class DatamarkCommand extends CliCommand {

    private static final String COMMAND_OPTION_LIST_SERVICES = "ls";
    private static final String COMMAND_OPTION_LIST_SERVICES_LONG_OPT = "list-services";
    private static final String COMMAND_OPTION_UPDATE_ALLOWED = "ua";
    private static final String COMMAND_OPTION_UPDATE_ALLOWED_LONG_OPT = "update-allowed";
    private static final String COMMAND_OPTION_GET = "get";
    private static final String COMMAND_OPTION_REFRESH = "refresh";
    private static final String COMMAND_OPTION_UPDATE = "update";

    private static final String[] COMMAND_OPTION_ARRAY = {
            COMMAND_OPTION_LIST_SERVICES,
            COMMAND_OPTION_UPDATE_ALLOWED,
            COMMAND_OPTION_GET,
            COMMAND_OPTION_REFRESH,
            COMMAND_OPTION_UPDATE
    };

    private static final String COMMAND_OPTION_SERVICE_ID = "sid";
    private static final String COMMAND_OPTION_DATAMARK = "dm";

    @SuppressWarnings({"SpellCheckingInspection", "RedundantSuppression"})
    private static final String IDENTITY = "datamark";
    private static final String DESCRIPTION = "数据标识服务";

    private static final String CMD_LINE_SYNTAX_LIST_SERVICES = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_LIST_SERVICES);
    private static final String CMD_LINE_SYNTAX_UPDATE_ALLOWED = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_UPDATE_ALLOWED) + " [" +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_SERVICE_ID) + " service-id]";
    private static final String CMD_LINE_SYNTAX_GET = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_GET) + " [" +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_SERVICE_ID) + " service-id]";
    private static final String CMD_LINE_SYNTAX_REFRESH = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_REFRESH) + " [" +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_SERVICE_ID) + " service-id]";
    private static final String CMD_LINE_SYNTAX_UPDATE = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_UPDATE) + " [" +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_SERVICE_ID) + " service-id] [" +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_DATAMARK) + " datamark]";

    private static final String[] CMD_LINE_ARRAY = new String[]{
            CMD_LINE_SYNTAX_LIST_SERVICES,
            CMD_LINE_SYNTAX_UPDATE_ALLOWED,
            CMD_LINE_SYNTAX_GET,
            CMD_LINE_SYNTAX_REFRESH,
            CMD_LINE_SYNTAX_UPDATE
    };

    private static final String CMD_LINE_SYNTAX = CommandUtil.syntax(CMD_LINE_ARRAY);

    private final Map<String, DatamarkService> datamarkServiceMap;

    private List<String> datamarkServiceIds;

    public DatamarkCommand(Map<String, DatamarkService> datamarkServiceMap) {
        super(IDENTITY, DESCRIPTION, CMD_LINE_SYNTAX);
        this.datamarkServiceMap = datamarkServiceMap;
    }

    @PostConstruct
    private void init() {
        datamarkServiceIds = datamarkServiceMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    protected List<Option> buildOptions() {
        List<Option> list = new ArrayList<>();
        list.add(
                Option.builder(COMMAND_OPTION_LIST_SERVICES).longOpt(COMMAND_OPTION_LIST_SERVICES_LONG_OPT)
                        .desc("列出所有可用的服务").build()
        );
        list.add(
                Option.builder(COMMAND_OPTION_UPDATE_ALLOWED).longOpt(COMMAND_OPTION_UPDATE_ALLOWED_LONG_OPT)
                        .desc("返回服务是否允许更新").build()
        );
        list.add(Option.builder(COMMAND_OPTION_GET).desc("获取数据标识").build());
        list.add(Option.builder(COMMAND_OPTION_REFRESH).desc("刷新数据标识").build());
        list.add(Option.builder(COMMAND_OPTION_UPDATE).desc("更新数据标识").build());
        list.add(Option.builder(COMMAND_OPTION_SERVICE_ID).desc("数据服务 ID").hasArg().type(String.class).build());
        list.add(Option.builder(COMMAND_OPTION_DATAMARK).desc("数据标识").hasArg().type(String.class).build());
        return list;
    }

    @Override
    protected void executeWithCmd(Context context, CommandLine commandLine) throws TelqosException {
        try {
            Pair<String, Integer> pair = CommandUtil.analyseCommand(commandLine, COMMAND_OPTION_ARRAY);
            if (pair.getRight() != 1) {
                context.sendMessage(CommandUtil.optionMismatchMessage(COMMAND_OPTION_ARRAY));
                context.sendMessage(super.cmdLineSyntax);
                return;
            }
            switch (pair.getLeft()) {
                case COMMAND_OPTION_LIST_SERVICES:
                    handleListServices(context, commandLine);
                    break;
                case COMMAND_OPTION_UPDATE_ALLOWED:
                    handleUpdateAllowed(context, commandLine);
                    break;
                case COMMAND_OPTION_GET:
                    handleGet(context, commandLine);
                    break;
                case COMMAND_OPTION_REFRESH:
                    handleRefresh(context, commandLine);
                    break;
                case COMMAND_OPTION_UPDATE:
                    handleUpdate(context, commandLine);
                    break;
            }
        } catch (Exception e) {
            throw new TelqosException(e);
        }
    }

    private void handleListServices(
            Context context,
            // 为了代码的可扩展性，此处不做简化
            @SuppressWarnings("unused") CommandLine commandLine
    ) throws Exception {
        // 输出结果。
        context.sendMessage("可用的服务 ID 列表: ");
        if (datamarkServiceIds.isEmpty()) {
            context.sendMessage("  (Empty)");
        } else {
            int index = 0;
            for (String datamarkServiceId : datamarkServiceIds) {
                context.sendMessage(String.format("  %3d: %s", ++index, datamarkServiceId));
            }
        }
    }

    @SuppressWarnings("DuplicatedCode")
    private void handleUpdateAllowed(
            Context context,
            // 为了代码的可扩展性，此处不做简化
            @SuppressWarnings("unused") CommandLine commandLine
    ) throws Exception {
        if (datamarkServiceIds.isEmpty()) {
            context.sendMessage("应用上下文中不存在任何 DatamarkService, 命令结束");
            return;
        }

        // 交互标记。
        boolean interactiveFlag = false;

        // 确定 serviceId。
        String serviceId = null;
        // 如果有 COMMAND_OPTION_SERVICE_ID 选项，则直接获取 serviceId。
        if (commandLine.hasOption(COMMAND_OPTION_SERVICE_ID)) {
            serviceId = commandLine.getOptionValue(COMMAND_OPTION_SERVICE_ID);
            if (!datamarkServiceMap.containsKey(serviceId)) {
                context.sendMessage("选项中的数据标识服务 ID 对应的数据标识服务不存在, 使用交互式输入...");
                serviceId = null;
            }
        }
        // 如果 serviceId 为 null，则使用交互式输入获取。
        if (Objects.isNull(serviceId)) {
            serviceId = interactiveGetServiceId(context);
            interactiveFlag = true;
        }

        // 调用服务，获取服务是否允许更新。
        boolean updateAllowed = datamarkServiceMap.get(serviceId).updateAllowed();

        // 信息输出。
        if (interactiveFlag) {
            context.sendMessage(StringUtils.EMPTY);
        }
        context.sendMessage("Service ID: " + serviceId + ", 允许更新: " + updateAllowed);
    }

    @SuppressWarnings("DuplicatedCode")
    private void handleGet(
            Context context,
            // 为了代码的可扩展性，此处不做简化
            @SuppressWarnings("unused") CommandLine commandLine
    ) throws Exception {
        if (datamarkServiceIds.isEmpty()) {
            context.sendMessage("应用上下文中不存在任何 DatamarkService, 命令结束");
            return;
        }

        // 交互标记。
        boolean interactiveFlag = false;

        // 确定 serviceId。
        String serviceId = null;
        // 如果有 COMMAND_OPTION_SERVICE_ID 选项，则直接获取 serviceId。
        if (commandLine.hasOption(COMMAND_OPTION_SERVICE_ID)) {
            serviceId = commandLine.getOptionValue(COMMAND_OPTION_SERVICE_ID);
            if (!datamarkServiceMap.containsKey(serviceId)) {
                context.sendMessage("选项中的数据标识服务 ID 对应的数据标识服务不存在, 使用交互式输入...");
                serviceId = null;
            }
        }
        // 如果 serviceId 为 null，则使用交互式输入获取。
        if (Objects.isNull(serviceId)) {
            serviceId = interactiveGetServiceId(context);
            interactiveFlag = true;
        }

        // 调用服务，获取数据标识。
        String datamark = datamarkServiceMap.get(serviceId).get();

        // 信息输出。
        if (interactiveFlag) {
            context.sendMessage(StringUtils.EMPTY);
        }
        context.sendMessage("Service ID: " + serviceId + ", 数据标识: " + datamark);
    }

    @SuppressWarnings("DuplicatedCode")
    private void handleRefresh(
            Context context,
            // 为了代码的可扩展性，此处不做简化
            @SuppressWarnings("unused") CommandLine commandLine
    ) throws Exception {
        if (datamarkServiceIds.isEmpty()) {
            context.sendMessage("应用上下文中不存在任何 DatamarkService, 命令结束");
            return;
        }

        // 交互标记。
        boolean interactiveFlag = false;

        // 确定 serviceId。
        String serviceId = null;
        // 如果有 COMMAND_OPTION_SERVICE_ID 选项，则直接获取 serviceId。
        if (commandLine.hasOption(COMMAND_OPTION_SERVICE_ID)) {
            serviceId = commandLine.getOptionValue(COMMAND_OPTION_SERVICE_ID);
            if (!datamarkServiceMap.containsKey(serviceId)) {
                context.sendMessage("选项中的数据标识服务 ID 对应的数据标识服务不存在, 使用交互式输入...");
                serviceId = null;
            }
        }
        // 如果 serviceId 为 null，则使用交互式输入获取。
        if (Objects.isNull(serviceId)) {
            serviceId = interactiveGetServiceId(context);
            interactiveFlag = true;
        }

        // 调用服务，获取数据标识。
        String datamark = datamarkServiceMap.get(serviceId).refreshAndGet();

        // 信息输出。
        if (interactiveFlag) {
            context.sendMessage(StringUtils.EMPTY);
        }
        context.sendMessage("刷新成功!");
        context.sendMessage("Service ID: " + serviceId + ", 刷新后的数据标识: " + datamark);
    }

    @SuppressWarnings("DuplicatedCode")
    private void handleUpdate(Context context, CommandLine commandLine) throws Exception {
        if (datamarkServiceIds.isEmpty()) {
            context.sendMessage("应用上下文中不存在任何 DatamarkService, 命令结束");
            return;
        }

        // 交互标记。
        boolean interactiveFlag = false;

        // 确定 serviceId。
        String serviceId = null;
        // 如果有 COMMAND_OPTION_SERVICE_ID 选项，则直接获取 serviceId。
        if (commandLine.hasOption(COMMAND_OPTION_SERVICE_ID)) {
            serviceId = commandLine.getOptionValue(COMMAND_OPTION_SERVICE_ID);
            if (!datamarkServiceMap.containsKey(serviceId)) {
                context.sendMessage("选项中的数据标识服务 ID 对应的数据标识服务不存在, 使用交互式输入...");
                serviceId = null;
            }
        }
        // 如果 serviceId 为 null，则使用交互式输入获取。
        if (Objects.isNull(serviceId)) {
            serviceId = interactiveGetServiceId(context);
            interactiveFlag = true;
        }

        // 确定 datamark。
        String datamark = null;
        // 如果有 COMMAND_OPTION_DATAMARK 选项，则直接获取 datamark。
        if (commandLine.hasOption(COMMAND_OPTION_DATAMARK)) {
            datamark = StringUtils.trim((String) commandLine.getParsedOptionValue(COMMAND_OPTION_DATAMARK));
            if (!DatamarkUtil.isDatamarkValid(datamark)) {
                context.sendMessage("选项中的数据标识符不合法, 请重新输入, 使用交互式输入...");
                datamark = null;
            }
        }
        // 如果 datamark 为 null，则使用交互式输入获取。
        if (Objects.isNull(datamark)) {
            datamark = interactiveGetDatamark(context);
            interactiveFlag = true;
        }

        // 调用服务。
        datamarkServiceMap.get(serviceId).update(datamark);

        // 信息输出。
        if (interactiveFlag) {
            context.sendMessage(StringUtils.EMPTY);
        }
        context.sendMessage("更新成功!");
        context.sendMessage("Service ID: " + serviceId + ", 更新的数据标识: " + datamark);
    }

    private String interactiveGetServiceId(Context context) throws Exception {
        context.sendMessage("请输入数据标识服务的 ID, 请输入一个有效的数据标识服务 ID:");
        context.sendMessage("可用的服务 ID 列表: ");
        int index = 0;
        for (String datamarkServiceId : datamarkServiceIds) {
            context.sendMessage(String.format("  %3d: %s", ++index, datamarkServiceId));
        }
        String templateServiceId;
        int retryTimes = 0;
        while (true) {
            context.sendMessage("请输入数据标识服务的 ID:");
            templateServiceId = context.receiveMessage();
            if (!datamarkServiceMap.containsKey(templateServiceId)) {
                if (retryTimes < CommandUtil.MAX_INTERACTIVE_RETRY_TIMES) {
                    retryTimes++;
                    context.sendMessage("输入的数据标识服务 ID 对应的数据标识服务不存在, 请重新输入");
                    continue;
                } else {
                    throw new MaxInteractiveRetryTimesExceededException(retryTimes);
                }
            }
            break;
        }
        return templateServiceId;
    }

    private String interactiveGetDatamark(Context context) throws Exception {
        context.sendMessage("请输入新的数据标识, 如果新的数据标识是空字符串, 请输入空格");
        context.sendMessage("请输入合法的数据标识, 请参阅文档以获取数据标识的校验规则");
        String tempDatamark;
        int retryTimes = 0;
        while (true) {
            context.sendMessage("请输入新的数据标识:");
            tempDatamark = StringUtils.trim(context.receiveMessage());
            if (!DatamarkUtil.isDatamarkValid(tempDatamark)) {
                if (retryTimes < CommandUtil.MAX_INTERACTIVE_RETRY_TIMES) {
                    retryTimes++;
                    context.sendMessage("输入的数据标识符不合法, 参阅文档以获取数据标识的校验规则, 请重新输入");
                    continue;
                } else {
                    throw new MaxInteractiveRetryTimesExceededException(retryTimes);
                }
            }
            break;
        }
        return tempDatamark;
    }
}
