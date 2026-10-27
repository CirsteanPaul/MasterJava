package lab3.library.domain;

import java.time.LocalDate;

public record Loan(Long memberId, Long bookId, LocalDate borrowedOn, LocalDate dueOn) {
}
