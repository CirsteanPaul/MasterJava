package lab3.library.aop;

import lab3.library.domain.Book;
import lab3.library.domain.Member;
import lab3.library.repository.BookRepository;
import lab3.library.repository.MemberRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Logs every borrow / return: method name, member name, book title and execution time. Outermost aspect. */
@Aspect
@Component
@Order(1)
public class LoanLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoanLoggingAspect.class);

    private final MemberRepository members;
    private final BookRepository books;

    public LoanLoggingAspect(MemberRepository members, BookRepository books) {
        this.members = members;
        this.books = books;
    }

    @Around("lab3.library.aop.LibraryPointcuts.loanOperation() && args(memberId, bookId)")
    public Object logLoanOperation(ProceedingJoinPoint joinPoint, Long memberId, Long bookId) throws Throwable {
        String member = members.findById(memberId).map(Member::name).orElse("<unknown member #" + memberId + ">");
        String book = books.findById(bookId).map(Book::getTitle).orElse("<unknown book #" + bookId + ">");
        String outcome = "FAILED";
        long start = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            outcome = "OK";
            return result;
        } finally {
            log.info("[LOG] {}(member='{}', book='{}') -> {} in {} ms",
                    joinPoint.getSignature().getName(), member, book, outcome,
                    "%.3f".formatted((System.nanoTime() - start) / 1_000_000.0));
        }
    }
}
