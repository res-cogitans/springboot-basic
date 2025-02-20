package org.programmers.springbootbasic.console.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.springbootbasic.console.command.Command;
import org.programmers.springbootbasic.console.command.InputCommand;
import org.programmers.springbootbasic.console.model.ConsoleModelAndView;
import org.programmers.springbootbasic.console.request.ConsoleRequest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.programmers.springbootbasic.console.ConsoleResponseCode.PROCEED;
import static org.programmers.springbootbasic.console.ConsoleResponseCode.STOP;
import static org.programmers.springbootbasic.console.command.InputCommand.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CliHandler implements Handler {

    private static final Map<String, Command> commandList = new ConcurrentHashMap<>();

    @PostConstruct
    @Override
    public void initCommandList() {
        commandList.put(HELP.getViewName(), HELP);
        commandList.put(HOME.getViewName(), HOME);
        commandList.put(EXIT.getViewName(), EXIT);
    }

    @Override
    public boolean supports(Command command) {
        for (var supportingCommand : commandList.values()) {
            if (command == supportingCommand) {
                log.debug("Controller: {} supports command: {}.", this, command);
                return true;
            }
        }
        return false;
    }

    @Override
    public ConsoleModelAndView handleRequest(ConsoleRequest request) {
        var command = request.getCommand();
        log.info("processing command {} at Controller", command);

        if (command == HELP) {
            return help(request);
        }
        if (command == EXIT) {
            return processStaticPage(request, STOP);
        }
        return processStaticPage(request, PROCEED);
    }

    private ConsoleModelAndView help(ConsoleRequest request) {
        var model = request.getConsoleModel();

        var commands = InputCommand.values();
        List<String> allCommandsInformation = new ArrayList<>();

        for (InputCommand eachCommand : commands) {
            allCommandsInformation.add(eachCommand.getCommandInformation());
        }

        model.addAttributes("allCommandsInformation", allCommandsInformation);

        return new ConsoleModelAndView(model, request.getCommand().getViewName(), PROCEED);
    }
}
