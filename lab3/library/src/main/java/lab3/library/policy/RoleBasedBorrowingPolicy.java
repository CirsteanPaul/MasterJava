package lab3.library.policy;

import lab3.library.config.LibraryProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** The limit (and loan period) depends on the member's role: student / regular / premium. */
@Component
@ConditionalOnProperty(name = "library.borrowing.policy", havingValue = "role-based")
public class RoleBasedBorrowingPolicy extends LimitBasedPolicy {

    private final LibraryProperties.Borrowing config;

    public RoleBasedBorrowingPolicy(LibraryProperties properties) {
        this.config = properties.borrowing();
    }

    @Override
    public String name() {
        return "role based " + config.roleLimits();
    }

    @Override
    protected int maxBooks(BorrowingRequest request) {
        return config.roleLimits().getOrDefault(request.member().membershipType(), config.fixedLimit());
    }

    @Override
    protected int loanDays(BorrowingRequest request) {
        return config.roleLoanDays().getOrDefault(request.member().membershipType(), config.loanDays());
    }
}
