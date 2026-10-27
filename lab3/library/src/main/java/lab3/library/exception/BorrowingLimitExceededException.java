package lab3.library.exception;

public class BorrowingLimitExceededException extends LibraryException {
    public BorrowingLimitExceededException(String message) {
        super(message);
    }
}
