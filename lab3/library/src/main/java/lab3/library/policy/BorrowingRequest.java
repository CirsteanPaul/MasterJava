package lab3.library.policy;

import lab3.library.domain.Book;
import lab3.library.domain.Member;

/** Everything a policy needs to decide on a borrowing. */
public record BorrowingRequest(Member member, Book book, int currentlyBorrowed) {
}
