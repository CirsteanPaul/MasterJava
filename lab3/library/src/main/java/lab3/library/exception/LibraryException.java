package lab3.library.exception;

/** Base class of all library business errors. */
public abstract class LibraryException extends RuntimeException {
    public LibraryException(String message) {
        super(message);
    }
}
