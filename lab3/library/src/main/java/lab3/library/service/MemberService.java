package lab3.library.service;

import lab3.library.domain.Member;
import lab3.library.exception.MemberNotFoundException;
import lab3.library.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    private final MemberRepository members;

    public MemberService(MemberRepository members) {
        this.members = members;
    }

    public Member register(Member member) {
        return members.save(member);
    }

    public Member get(Long id) {
        return members.findById(id).orElseThrow(() -> new MemberNotFoundException("member #" + id + " does not exist"));
    }

    public List<Member> all() {
        return members.findAll();
    }
}
