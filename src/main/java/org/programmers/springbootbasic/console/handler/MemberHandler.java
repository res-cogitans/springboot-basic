package org.programmers.springbootbasic.console.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.springbootbasic.console.command.Command;
import org.programmers.springbootbasic.console.model.ConsoleModelAndView;
import org.programmers.springbootbasic.console.request.ConsoleRequest;
import org.programmers.springbootbasic.member.service.MemberService;
import org.programmers.springbootbasic.web.dto.MemberDto;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.programmers.springbootbasic.console.ConsoleResponseCode.INPUT_AND_REDIRECT;
import static org.programmers.springbootbasic.console.ConsoleResponseCode.PROCEED;
import static org.programmers.springbootbasic.console.command.InputCommand.CREATE_MEMBER;
import static org.programmers.springbootbasic.console.command.InputCommand.LIST_MEMBER;
import static org.programmers.springbootbasic.console.command.RedirectCommand.CREATE_MEMBER_COMPLETE;
import static org.programmers.springbootbasic.console.command.RedirectCommand.CREATE_MEMBER_EMAIL;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberHandler implements Handler {

    private final MemberService memberService;
    private static final String VIEW_PATH = "member/";
    private static final Map<String, Command> COMMANDS = new ConcurrentHashMap<>();

    @PostConstruct
    @Override
    public void initCommandList() {
        COMMANDS.put(CREATE_MEMBER.getViewName(), CREATE_MEMBER);
        COMMANDS.put(CREATE_MEMBER_EMAIL.getViewName(), CREATE_MEMBER_EMAIL);
        COMMANDS.put(CREATE_MEMBER_COMPLETE.getViewName(), CREATE_MEMBER_COMPLETE);
        COMMANDS.put(LIST_MEMBER.getViewName(), LIST_MEMBER);
    }

    @Override
    public boolean supports(Command command) {
        for (var supportingCommand : COMMANDS.values()) {
            if (command == supportingCommand) {
                log.trace("Controller: {} supports command: {}.", this, command);
                return true;
            }
        }
        return false;
    }

    @Override
    public ConsoleModelAndView handleRequest(ConsoleRequest request) {
        var command = request.getCommand();
        log.info("processing command {} at Controller", command);

        if (command == CREATE_MEMBER) {
            return create(request);
        }
        if (command == CREATE_MEMBER_EMAIL) {
            return createEmail(request);
        }
        if (command == CREATE_MEMBER_COMPLETE) {
            return createComplete(request);
        }
        if (command == LIST_MEMBER) {
            return list(request);
        }
        throw new IllegalStateException(
                "컨트롤러가 해당 커맨드를 처리하지 못 합니다. 컨트롤러 매핑이 잘못되었는지 확인해주세요.");
    }

    private ConsoleModelAndView create(ConsoleRequest request) {
        var model = request.getConsoleModel();

        model.setInputSignature("name");
        model.setRedirectLink(CREATE_MEMBER_EMAIL);

        return new ConsoleModelAndView(model, VIEW_PATH + request.getCommand().getViewName(), INPUT_AND_REDIRECT);
    }

    private ConsoleModelAndView createEmail(ConsoleRequest request) {
        var model = request.getConsoleModel();

        model.setInputSignature("email");
        model.setRedirectLink(CREATE_MEMBER_COMPLETE);

        return new ConsoleModelAndView(model, VIEW_PATH + request.getCommand().getViewName(), INPUT_AND_REDIRECT);
    }

    private ConsoleModelAndView createComplete(ConsoleRequest request) {
        var model = request.getConsoleModel();

        String name = (String) model.getAttributes("name");
        String email = (String) model.getAttributes("email");

        memberService.signUp(name, email);

        model.clear();

        return new ConsoleModelAndView(model, VIEW_PATH + request.getCommand().getViewName(), PROCEED);
    }

    private ConsoleModelAndView list(ConsoleRequest request) {
        var model = request.getConsoleModel();

        List<MemberDto> members = memberService.getAllMembers();
        if (members.isEmpty()) {
            model.addAttributes("allMembers", "가입된 회원이 없습니다.");
            return new ConsoleModelAndView(model, request.getCommand().getViewName(), PROCEED);
        }
        List<String> allMembersInformation = new ArrayList<>(members.size());

        for (MemberDto member : members) {
            allMembersInformation.add(member.toString());
        }

        model.addAttributes("allMembers", allMembersInformation);

        return new ConsoleModelAndView(model, VIEW_PATH + request.getCommand().getViewName(), PROCEED);
    }
}