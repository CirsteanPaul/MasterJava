package lab3.library.exception;

/** Thrown when the borrowing policy rejects a request. */
public class BorrowingNotAllowedException extends LibraryException {
    public BorrowingNotAllowedException(String message) {
        super(message);
    }
}
