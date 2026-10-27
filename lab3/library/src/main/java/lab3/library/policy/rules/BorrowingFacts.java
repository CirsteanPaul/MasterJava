package lab3.library.policy.rules;

import lab3.library.domain.BookType;
import lab3.library.domain.MembershipType;

import java.util.ArrayList;
import java.util.List;

/**
 * The fact inserted into the Drools session. Inputs are read-only; rules fill in
 * {@code maxBooks}, {@code loanDays} and call {@link #deny(String)}.
 */
public class BorrowingFacts {

    private final String memberName;
    private final MembershipType membershipType;
    private final int currentlyBorrowed;
    private final String bookTitle;
    private final BookType bookType;
    private final boolean bookAvailable;

    private Integer maxBooks;
    private Integer loanDays;
    private final List<String> denialReasons = new ArrayList<>();

    public BorrowingFacts(String memberName, MembershipType membershipType, int currentlyBorrowed,
                          String bookTitle, BookType bookType, boolean bookAvailable) {
        this.memberName = memberName;
        this.membershipType = membershipType;
        this.currentlyBorrowed = currentlyBorrowed;
        this.bookTitle = bookTitle;
        this.bookType = bookType;
        this.bookAvailable = bookAvailable;
    }

    public void deny(String reason) {
        denialReasons.add(reason);
    }

    public boolean isDenied() { return !denialReasons.isEmpty(); }
    public String getMemberName() { return memberName; }
    public MembershipType getMembershipType() { return membershipType; }
    public int getCurrentlyBorrowed() { return currentlyBorrowed; }
    public String getBookTitle() { return bookTitle; }
    public BookType getBookType() { return bookType; }
    public boolean isBookAvailable() { return bookAvailable; }
    public Integer getMaxBooks() { return maxBooks; }
    public void setMaxBooks(Integer maxBooks) { this.maxBooks = maxBooks; }
    public Integer getLoanDays() { return loanDays; }
    public void setLoanDays(Integer loanDays) { this.loanDays = loanDays; }
    public List<String> getDenialReasons() { return List.copyOf(denialReasons); }
}
