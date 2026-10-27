package lab3.library;

import lab3.library.domain.Book;
import lab3.library.domain.BookType;
import lab3.library.domain.Loan;
import lab3.library.domain.Member;
import lab3.library.domain.MembershipType;
import lab3.library.event.HeavyBorrowerListener;
import lab3.library.exception.BookNotAvailableException;
import lab3.library.exception.BookNotFoundException;
import lab3.library.exception.BorrowingLimitExceededException;
import lab3.library.exception.LoanNotFoundException;
import lab3.library.exception.MemberNotFoundException;
import lab3.library.service.BookService;
import lab3.library.service.BorrowingService;
import lab3.library.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "library.demo.enabled=false",
        "library.borrowing.policy=role-based",
        "library.events.heavy-borrower-threshold=2"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith(OutputCaptureExtension.class)
class BorrowingServiceTest {

    @Autowired MemberService members;
    @Autowired BookService books;
    @Autowired BorrowingService borrowing;
    @Autowired HeavyBorrowerListener listener;

    @BeforeEach
    void seed() {
        members.register(new Member(1L, "Stella Student", MembershipType.STUDENT));
        members.register(new Member(2L, "Petra Premium", MembershipType.PREMIUM));
        for (long id = 1; id <= 5; id++) {
            books.add(new Book(id, "Book " + id, "Author", BookType.STANDARD));
        }
    }

    @Test
    void serviceIsProxiedByAspects() {
        assertThat(AopUtils.isAopProxy(borrowing)).isTrue();
    }

    @Test
    void borrowsWithRoleSpecificLoanPeriod() {
        Loan loan = borrowing.borrowBook(2L, 1L);

        assertThat(loan.dueOn()).isEqualTo(LocalDate.now().plusDays(30));
        assertThat(books.get(1L).isAvailable()).isFalse();
    }

    @Test
    void validationAspectRejectsUnknownMemberAndBook() {
        assertThatThrownBy(() -> borrowing.borrowBook(99L, 1L)).isInstanceOf(MemberNotFoundException.class);
        assertThatThrownBy(() -> borrowing.borrowBook(1L, 99L)).isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void validationAspectRejectsUnavailableBook() {
        borrowing.borrowBook(2L, 1L);

        assertThatThrownBy(() -> borrowing.borrowBook(1L, 1L))
                .isInstanceOf(BookNotAvailableException.class);
    }

    @Test
    void validationAspectEnforcesRoleLimit() {
        borrowing.borrowBook(1L, 1L);
        borrowing.borrowBook(1L, 2L);

        assertThatThrownBy(() -> borrowing.borrowBook(1L, 3L))
                .isInstanceOf(BorrowingLimitExceededException.class)
                .hasMessageContaining("2/2");
    }

    @Test
    void returningMakesTheBookAvailableAgain() {
        borrowing.borrowBook(1L, 1L);
        borrowing.returnBook(1L, 1L);

        assertThat(books.get(1L).isAvailable()).isTrue();
        assertThatThrownBy(() -> borrowing.returnBook(1L, 1L)).isInstanceOf(LoanNotFoundException.class);
    }

    @Test
    void publishesEventAboveThreshold() {
        borrowing.borrowBook(2L, 1L);
        borrowing.borrowBook(2L, 2L);
        assertThat(listener.received()).isEmpty();

        borrowing.borrowBook(2L, 3L);

        assertThat(listener.received()).singleElement().satisfies(e -> {
            assertThat(e.member().name()).isEqualTo("Petra Premium");
            assertThat(e.booksBorrowed()).isEqualTo(3);
        });
    }

    @Test
    void loggingAndErrorAspectsWriteTheLog(CapturedOutput output) {
        borrowing.borrowBook(1L, 1L);
        try {
            borrowing.borrowBook(99L, 2L);
        } catch (MemberNotFoundException ignored) {
            // logged by the aspect
        }

        assertThat(output)
                .containsPattern("\\[LOG] borrowBook\\(member='Stella Student', book='Book 1'\\) -> OK in [\\d.]+ ms")
                .contains("[LOG] borrowBook(member='<unknown member #99>', book='Book 2') -> FAILED")
                .contains("[ERROR] operation BorrowingService.borrowBook[99, 2] failed with MemberNotFoundException");
    }
}
