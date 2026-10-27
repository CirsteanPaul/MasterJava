package lab3.library.policy;

import java.util.List;

/**
 * Outcome of a {@link BorrowingPolicy}.
 *
 * @param allowed  whether the member may borrow the book
 * @param maxBooks how many books the member may hold at the same time
 * @param loanDays borrowing period (only meaningful when allowed)
 * @param reasons  why the request was rejected (empty when allowed)
 */
public record BorrowingDecision(boolean allowed, int maxBooks, int loanDays, List<String> reasons) {

    public BorrowingDecision {
        reasons = List.copyOf(reasons);
    }

    public static BorrowingDecision allow(int maxBooks, int loanDays) {
        return new BorrowingDecision(true, maxBooks, loanDays, List.of());
    }

    public static BorrowingDecision deny(int maxBooks, List<String> reasons) {
        return new BorrowingDecision(false, maxBooks, 0, reasons);
    }

    public boolean limitReached(int currentlyBorrowed) {
        return currentlyBorrowed >= maxBooks;
    }
}
