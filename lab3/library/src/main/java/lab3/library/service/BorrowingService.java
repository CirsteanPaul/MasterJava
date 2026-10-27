package lab3.library.service;

import lab3.library.config.LibraryProperties;
import lab3.library.domain.Book;
import lab3.library.domain.Loan;
import lab3.library.domain.Member;
import lab3.library.event.HeavyBorrowerEvent;
import lab3.library.exception.BorrowingNotAllowedException;
import lab3.library.exception.LoanNotFoundException;
import lab3.library.policy.BorrowingDecision;
import lab3.library.policy.BorrowingPolicy;
import lab3.library.policy.BorrowingRequest;
import lab3.library.repository.LoanRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Processes borrowings and returns. It only knows the {@link BorrowingPolicy} abstraction: which
 * implementation is used (fixed, role-based, rule engine) is decided by configuration.
 * Pre-validation, logging and error reporting are added by aspects (see {@code lab3.library.aop}).
 */
@Service
public class BorrowingService {

    private final MemberService members;
    private final BookService books;
    private final LoanRepository loans;
    private final BorrowingPolicy policy;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final int heavyBorrowerThreshold;

    public BorrowingService(MemberService members, BookService books, LoanRepository loans,
                            BorrowingPolicy policy, ApplicationEventPublisher events,
                            Clock clock, LibraryProperties properties) {
        this.members = members;
        this.books = books;
        this.loans = loans;
        this.policy = policy;
        this.events = events;
        this.clock = clock;
        this.heavyBorrowerThreshold = properties.events().heavyBorrowerThreshold();
    }

    public synchronized Loan borrowBook(Long memberId, Long bookId) {
        Member member = members.get(memberId);
        Book book = books.get(bookId);
        int current = loans.countActiveByMember(memberId);

        BorrowingDecision decision = policy.evaluate(new BorrowingRequest(member, book, current));
        if (!decision.allowed()) {
            throw new BorrowingNotAllowedException(member.name() + " cannot borrow " + book.getTitle()
                    + ": " + String.join("; ", decision.reasons()));
        }

        LocalDate today = LocalDate.now(clock);
        book.setAvailable(false);
        Loan loan = loans.save(new Loan(memberId, bookId, today, today.plusDays(decision.loanDays())));

        int nowBorrowed = current + 1;
        if (nowBorrowed > heavyBorrowerThreshold) {
            events.publishEvent(new HeavyBorrowerEvent(member, nowBorrowed, heavyBorrowerThreshold));
        }
        return loan;
    }

    public synchronized Loan returnBook(Long memberId, Long bookId) {
        Member member = members.get(memberId);
        Book book = books.get(bookId);
        Loan loan = loans.findActive(memberId, bookId).orElseThrow(() ->
                new LoanNotFoundException(member.name() + " has not borrowed " + book.getTitle()));
        loans.delete(loan);
        book.setAvailable(true);
        return loan;
    }

    public List<Loan> loansOf(Long memberId) {
        return loans.findActiveByMember(memberId);
    }

    public String policyName() {
        return policy.name();
    }
}
