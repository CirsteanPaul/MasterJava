package lab3.library.repository;

import lab3.library.domain.Loan;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryLoanRepository implements LoanRepository {

    private final List<Loan> loans = new CopyOnWriteArrayList<>();

    @Override
    public Loan save(Loan loan) {
        loans.add(loan);
        return loan;
    }

    @Override
    public Optional<Loan> findActive(Long memberId, Long bookId) {
        return loans.stream()
                .filter(l -> l.memberId().equals(memberId) && l.bookId().equals(bookId))
                .findFirst();
    }

    @Override
    public void delete(Loan loan) {
        loans.remove(loan);
    }

    @Override
    public List<Loan> findActiveByMember(Long memberId) {
        return loans.stream().filter(l -> l.memberId().equals(memberId)).toList();
    }
}
