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

import java.util.ArrayList;
import java.util.List;

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

    private static final String COMMAND_OPTION_UPDATE_ALLOWED = "ua";
    private static final String COMMAND_OPTION_UPDATE_ALLOWED_LONG_OPT = "update-allowed";
    private static final String COMMAND_OPTION_GET = "get";
    private static final String COMMAND_OPTION_REFRESH = "refresh";
    private static final String COMMAND_OPTION_UPDATE = "update";

    private static final String[] COMMAND_OPTION_ARRAY = {
            COMMAND_OPTION_UPDATE_ALLOWED,
            COMMAND_OPTION_GET,
            COMMAND_OPTION_REFRESH,
            COMMAND_OPTION_UPDATE
    };

    private static final String COMMAND_OPTION_DATAMARK = "dm";

    @SuppressWarnings({"SpellCheckingInspection", "RedundantSuppression"})
    private static final String IDENTITY = "datamark";
    private static final String DESCRIPTION = "数据标识服务";

    private static final String CMD_LINE_SYNTAX_UPDATE_ALLOWED = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_UPDATE_ALLOWED);
    private static final String CMD_LINE_SYNTAX_GET = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_GET);
    private static final String CMD_LINE_SYNTAX_REFRESH = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_REFRESH);
    private static final String CMD_LINE_SYNTAX_UPDATE = IDENTITY + " " +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_UPDATE) + " [" +
            CommandUtil.concatOptionPrefix(COMMAND_OPTION_DATAMARK) + " datamark]";

    private static final String[] CMD_LINE_ARRAY = new String[]{
            CMD_LINE_SYNTAX_UPDATE_ALLOWED,
            CMD_LINE_SYNTAX_GET,
            CMD_LINE_SYNTAX_REFRESH,
            CMD_LINE_SYNTAX_UPDATE
    };

    private static final String CMD_LINE_SYNTAX = CommandUtil.syntax(CMD_LINE_ARRAY);

    private final DatamarkService datamarkService;

    public DatamarkCommand(DatamarkService datamarkService) {
        super(IDENTITY, DESCRIPTION, CMD_LINE_SYNTAX);
        this.datamarkService = datamarkService;
    }

    @Override
    protected List<Option> buildOptions() {
        List<Option> list = new ArrayList<>();
        list.add(
                Option.builder(COMMAND_OPTION_UPDATE_ALLOWED).longOpt(COMMAND_OPTION_UPDATE_ALLOWED_LONG_OPT)
                        .desc("返回服务是否允许更新").build()
        );
        list.add(Option.builder(COMMAND_OPTION_GET).desc("获取数据标识").build());
        list.add(Option.builder(COMMAND_OPTION_REFRESH).desc("刷新数据标识").build());
        list.add(Option.builder(COMMAND_OPTION_UPDATE).desc("更新数据标识").build());
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

    private void handleUpdateAllowed(
            Context context,
            // 为了代码的可扩展性，此处不做简化
            @SuppressWarnings("unused") CommandLine commandLine
    ) throws Exception {
        // 调用服务，获取结果。
        boolean result = datamarkService.updateAllowed();
        // 输出结果。
        context.sendMessage("允许更新: " + result);
    }

    private void handleGet(
            Context context,
            // 为了代码的可扩展性，此处不做简化
            @SuppressWarnings("unused") CommandLine commandLine
    ) throws Exception {
        // 调用服务，获取结果。
        String result = datamarkService.get();
        // 输出结果。
        context.sendMessage("数据标识: " + result);
    }

    private void handleRefresh(
            Context context,
            // 为了代码的可扩展性，此处不做简化
            @SuppressWarnings("unused") CommandLine commandLine
    ) throws Exception {
        // 调用服务，获取结果。
        String result = datamarkService.refreshAndGet();
        // 输出结果。
        context.sendMessage("刷新成功!");
        context.sendMessage("数据标识: " + result);
    }

    private void handleUpdate(Context context, CommandLine commandLine) throws Exception {
        String datamark;
        // 如果有 COMMAND_OPTION_DATAMARK 选项，则直接获取 datamark。
        if (commandLine.hasOption(COMMAND_OPTION_DATAMARK)) {
            datamark = (String) commandLine.getParsedOptionValue(COMMAND_OPTION_DATAMARK);
        }
        // 交互式获取 datamark;
        else {
            datamark = interactiveGetDatamark(context);
        }
        // 调用服务。
        datamarkService.update(datamark);
        // 输出信息。
        context.sendMessage("更新成功!");
    }

    private String interactiveGetDatamark(Context context) throws Exception {
        context.sendMessage("请输入新的数据标识, 如果新的数据标识是空字符串, 请输入空格");
        context.sendMessage("请输入合法的数据标识, 请参阅文档以获取数据标识的校验规则");
        String tempDatamark;
        while (true) {
            context.sendMessage("请输入新的数据标识:");
            tempDatamark = StringUtils.trim(context.receiveMessage());
            if (!DatamarkUtil.isDatamarkValid(tempDatamark)) {
                context.sendMessage("数据标识符不合法, 请重新输入, 参阅文档以获取数据标识的校验规则");
                continue;
            }
            break;
        }
        return tempDatamark;
    }
}
