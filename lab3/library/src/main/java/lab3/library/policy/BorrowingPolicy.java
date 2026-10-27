package lab3.library.policy;

/**
 * Pluggable borrowing rules. Exactly one implementation is active, chosen with
 * {@code library.borrowing.policy} (fixed | role-based | rules).
 */
public interface BorrowingPolicy {

    String name();

    BorrowingDecision evaluate(BorrowingRequest request);
}
