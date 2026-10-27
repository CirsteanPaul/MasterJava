package lab3.library.aop;

import lab3.library.domain.Book;
import lab3.library.domain.Member;
import lab3.library.exception.BookNotAvailableException;
import lab3.library.exception.BookNotFoundException;
import lab3.library.exception.BorrowingLimitExceededException;
import lab3.library.exception.MemberNotFoundException;
import lab3.library.policy.BorrowingDecision;
import lab3.library.policy.BorrowingPolicy;
import lab3.library.policy.BorrowingRequest;
import lab3.library.repository.BookRepository;
import lab3.library.repository.LoanRepository;
import lab3.library.repository.MemberRepository;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Before a book is borrowed, checks that the member exists, the book is available and the member
 * has not reached the limit of the active policy. Violations raise a custom exception.
 */
@Aspect
@Component
@Order(3)
public class BorrowingValidationAspect {

    private final MemberRepository members;
    private final BookRepository books;
    private final LoanRepository loans;
    private final BorrowingPolicy policy;

    public BorrowingValidationAspect(MemberRepository members, BookRepository books,
                                     LoanRepository loans, BorrowingPolicy policy) {
        this.members = members;
        this.books = books;
        this.loans = loans;
        this.policy = policy;
    }

    @Before("lab3.library.aop.LibraryPointcuts.borrow() && args(memberId, bookId)")
    public void validate(Long memberId, Long bookId) {
        Member member = members.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("member #" + memberId + " does not exist"));
        Book book = books.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException("book #" + bookId + " does not exist"));
        if (!book.isAvailable()) {
            throw new BookNotAvailableException("'" + book.getTitle() + "' is already borrowed");
        }
        int current = loans.countActiveByMember(memberId);
        BorrowingDecision decision = policy.evaluate(new BorrowingRequest(member, book, current));
        if (decision.limitReached(current)) {
            throw new BorrowingLimitExceededException(
                    member.name() + " already holds " + current + "/" + decision.maxBooks() + " books");
        }
    }
}
