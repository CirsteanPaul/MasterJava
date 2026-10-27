package lab3.library.event;

import lab3.library.domain.Member;

/** Published when a member holds more books than {@code library.events.heavy-borrower-threshold}. */
public record HeavyBorrowerEvent(Member member, int booksBorrowed, int threshold) {
}
