package lab3.library.policy;

import lab3.library.config.LibraryProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Default policy: every member may hold the same number of books. */
@Component
@ConditionalOnProperty(name = "library.borrowing.policy", havingValue = "fixed", matchIfMissing = true)
public class FixedLimitBorrowingPolicy extends LimitBasedPolicy {

    private final LibraryProperties.Borrowing config;

    public FixedLimitBorrowingPolicy(LibraryProperties properties) {
        this.config = properties.borrowing();
    }

    @Override
    public String name() {
        return "fixed limit (" + config.fixedLimit() + " books)";
    }

    @Override
    protected int maxBooks(BorrowingRequest request) {
        return config.fixedLimit();
    }

    @Override
    protected int loanDays(BorrowingRequest request) {
        return config.loanDays();
    }
}
