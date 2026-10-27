package lab3.library.policy;

import java.util.ArrayList;
import java.util.List;

/** Shared logic of the simple, limit-only policies. */
abstract class LimitBasedPolicy implements BorrowingPolicy {

    protected abstract int maxBooks(BorrowingRequest request);

    protected abstract int loanDays(BorrowingRequest request);

    @Override
    public BorrowingDecision evaluate(BorrowingRequest request) {
        int max = maxBooks(request);
        List<String> reasons = new ArrayList<>();
        if (!request.book().isAvailable()) {
            reasons.add("book " + request.book().getTitle() + " is not available");
        }
        if (request.currentlyBorrowed() >= max) {
            reasons.add("limit of " + max + " books reached");
        }
        return reasons.isEmpty() ? BorrowingDecision.allow(max, loanDays(request)) : BorrowingDecision.deny(max, reasons);
    }
}
