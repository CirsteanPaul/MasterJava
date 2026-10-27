package lab3.library;

import lab3.library.config.LibraryProperties;
import lab3.library.domain.Book;
import lab3.library.domain.BookType;
import lab3.library.domain.Member;
import lab3.library.domain.MembershipType;
import lab3.library.policy.BorrowingDecision;
import lab3.library.policy.BorrowingRequest;
import lab3.library.policy.rules.RuleEngineBorrowingPolicy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineBorrowingPolicyTest {

    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final RuleEngineBorrowingPolicy policy = policy("classpath*:rules/*.drl", false);

    private static final Member STUDENT = new Member(1L, "S", MembershipType.STUDENT);
    private static final Member REGULAR = new Member(2L, "R", MembershipType.REGULAR);
    private static final Member PREMIUM = new Member(3L, "P", MembershipType.PREMIUM);

    @Test
    void limitDependsOnMembership() {
        assertThat(evaluate(STUDENT, BookType.STANDARD, 2).maxBooks()).isEqualTo(3);
        assertThat(evaluate(REGULAR, BookType.STANDARD, 0).maxBooks()).isEqualTo(5);
        assertThat(evaluate(PREMIUM, BookType.STANDARD, 0).maxBooks()).isEqualTo(10);
        assertThat(evaluate(STUDENT, BookType.STANDARD, 3).reasons()).anyMatch(r -> r.contains("limit of 3"));
    }

    @Test
    void periodDependsOnMembershipAndBookType() {
        assertThat(evaluate(STUDENT, BookType.STANDARD, 0).loanDays()).isEqualTo(14);
        assertThat(evaluate(REGULAR, BookType.STANDARD, 0).loanDays()).isEqualTo(21);
        assertThat(evaluate(PREMIUM, BookType.STANDARD, 0).loanDays()).isEqualTo(30);
        assertThat(evaluate(PREMIUM, BookType.NEW_RELEASE, 0).loanDays()).isEqualTo(7);
        assertThat(evaluate(PREMIUM, BookType.RARE, 0).loanDays()).isEqualTo(10);
    }

    @Test
    void bookTypeRestrictions() {
        assertThat(evaluate(PREMIUM, BookType.REFERENCE, 0).allowed()).isFalse();
        assertThat(evaluate(REGULAR, BookType.RARE, 0).allowed()).isFalse();
        assertThat(evaluate(PREMIUM, BookType.RARE, 0).allowed()).isTrue();
        assertThat(evaluate(STUDENT, BookType.NEW_RELEASE, 1).allowed()).isTrue();
        assertThat(evaluate(STUDENT, BookType.NEW_RELEASE, 2).allowed()).isFalse();
    }

    @Test
    void unavailableBookIsRejected() {
        Book book = new Book(1L, "Taken", "A", BookType.STANDARD);
        book.setAvailable(false);

        assertThat(policy.evaluate(new BorrowingRequest(PREMIUM, book, 0)).reasons())
                .containsExactly("'Taken' is not available");
    }

    @Test
    void rulesCanBeAddedAtRuntimeWithoutCodeChanges(@TempDir Path dir) throws IOException {
        for (Resource drl : resolver.getResources("classpath*:rules/*.drl")) {
            try (InputStream in = drl.getInputStream()) {
                Files.copy(in, dir.resolve(drl.getFilename()));
            }
        }
        RuleEngineBorrowingPolicy external = policy(dir.toUri() + "*.drl", true);
        assertThat(evaluate(external, STUDENT, BookType.STANDARD, 0).loanDays()).isEqualTo(14);

        Files.writeString(dir.resolve("04-exam-session.drl"), """
                package lab3.library.rules;
                import lab3.library.policy.rules.BorrowingFacts;
                import lab3.library.domain.MembershipType;

                rule "Exam session: students keep books at most 3 days"
                when
                    $f : BorrowingFacts(membershipType == MembershipType.STUDENT, loanDays != null, loanDays > 3)
                then
                    modify($f) { setLoanDays(3) }
                end
                """);

        assertThat(evaluate(external, STUDENT, BookType.STANDARD, 0).loanDays()).isEqualTo(3);
        assertThat(evaluate(external, REGULAR, BookType.STANDARD, 0).loanDays()).isEqualTo(21);
    }

    private BorrowingDecision evaluate(Member member, BookType type, int borrowed) {
        return evaluate(policy, member, type, borrowed);
    }

    private static BorrowingDecision evaluate(RuleEngineBorrowingPolicy policy, Member member, BookType type, int borrowed) {
        return policy.evaluate(new BorrowingRequest(member, new Book(9L, "Some book", "A", type), borrowed));
    }

    private RuleEngineBorrowingPolicy policy(String location, boolean autoReload) {
        LibraryProperties properties = new LibraryProperties(
                new LibraryProperties.Borrowing(LibraryProperties.PolicyType.RULES, 3, 14, Map.of(), Map.of()),
                new LibraryProperties.Events(3),
                new LibraryProperties.Rules(location, autoReload));
        return new RuleEngineBorrowingPolicy(resolver, properties);
    }
}
