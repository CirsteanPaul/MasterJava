package lab3.library.repository;

import lab3.library.domain.Member;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryMemberRepository implements MemberRepository {

    private final Map<Long, Member> members = new ConcurrentHashMap<>();

    @Override
    public Member save(Member member) {
        members.put(member.id(), member);
        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(id).map(members::get);
    }

    @Override
    public List<Member> findAll() {
        return List.copyOf(members.values());
    }
}
