package lab3.library;

import lab3.library.domain.Book;
import lab3.library.domain.BookType;
import lab3.library.domain.Member;
import lab3.library.domain.MembershipType;
import lab3.library.exception.LibraryException;
import lab3.library.service.BookService;
import lab3.library.service.BorrowingService;
import lab3.library.service.MemberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Seeds a small library and runs a scenario that exercises policies, aspects and events. */
@Component
@ConditionalOnProperty(name = "library.demo.enabled", havingValue = "true", matchIfMissing = true)
public class DemoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoRunner.class);

    private final MemberService members;
    private final BookService books;
    private final BorrowingService borrowing;

    public DemoRunner(MemberService members, BookService books, BorrowingService borrowing) {
        this.members = members;
        this.books = books;
        this.borrowing = borrowing;
    }

    @Override
    public void run(String... args) {
        members.register(new Member(1L, "Ana (student)", MembershipType.STUDENT));
        members.register(new Member(2L, "Bogdan (regular)", MembershipType.REGULAR));
        members.register(new Member(3L, "Carmen (premium)", MembershipType.PREMIUM));

        books.add(new Book(1L, "Clean Code", "Robert C. Martin", BookType.STANDARD));
        books.add(new Book(2L, "Effective Java", "Joshua Bloch", BookType.STANDARD));
        books.add(new Book(3L, "Spring in Action", "Craig Walls", BookType.STANDARD));
        books.add(new Book(4L, "Domain-Driven Design", "Eric Evans", BookType.STANDARD));
        books.add(new Book(5L, "Java Concurrency in Practice", "Brian Goetz", BookType.STANDARD));
        books.add(new Book(6L, "The Pragmatic Programmer, 20th ed.", "Thomas & Hunt", BookType.NEW_RELEASE));
        books.add(new Book(7L, "Oxford English Dictionary", "OUP", BookType.REFERENCE));
        books.add(new Book(8L, "Principia Mathematica (1687)", "Isaac Newton", BookType.RARE));

        log.info("=== Active borrowing policy: {} ===", borrowing.policyName());

        step("Ana borrows 'Clean Code'", () -> borrowing.borrowBook(1L, 1L));
        step("Ana borrows 'Effective Java'", () -> borrowing.borrowBook(1L, 2L));
        step("Ana borrows a new release", () -> borrowing.borrowBook(1L, 6L));
        step("Ana borrows 'Spring in Action'", () -> borrowing.borrowBook(1L, 3L));
        step("Ana borrows 'Domain-Driven Design'", () -> borrowing.borrowBook(1L, 4L));
        step("Bogdan borrows 'Clean Code' (already borrowed)", () -> borrowing.borrowBook(2L, 1L));
        step("Unknown member #99 borrows", () -> borrowing.borrowBook(99L, 5L));
        step("Bogdan borrows the dictionary (reference)", () -> borrowing.borrowBook(2L, 7L));
        step("Bogdan borrows the rare Principia", () -> borrowing.borrowBook(2L, 8L));
        step("Carmen borrows the rare Principia", () -> borrowing.borrowBook(3L, 8L));
        step("Ana returns 'Clean Code'", () -> borrowing.returnBook(1L, 1L));
        step("Bogdan returns a book he never borrowed", () -> borrowing.returnBook(2L, 2L));
        step("Bogdan borrows 'Clean Code'", () -> borrowing.borrowBook(2L, 1L));

        members.all().forEach(m -> log.info("{} holds {}", m.name(), borrowing.loansOf(m.id())));
    }

    private void step(String description, Runnable action) {
        log.info("--- {}", description);
        try {
            action.run();
        } catch (LibraryException e) {
            log.info("    rejected: {}", e.getMessage());
        }
    }
}
