package org.programmers.springbootbasic.member.service;

import org.programmers.springbootbasic.member.domain.Member;
import org.programmers.springbootbasic.web.dto.MemberDto;

import java.util.List;

public interface MemberService {
    Member signUp(String name, String email);

    List<MemberDto> getAllMembers();
}
