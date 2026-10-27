package lab3.library.repository;

import lab3.library.domain.Loan;

import java.util.List;
import java.util.Optional;

/** Keeps the active (not yet returned) loans. */
public interface LoanRepository {
    Loan save(Loan loan);
    Optional<Loan> findActive(Long memberId, Long bookId);
    void delete(Loan loan);
    List<Loan> findActiveByMember(Long memberId);

    default int countActiveByMember(Long memberId) {
        return findActiveByMember(memberId).size();
    }
}
