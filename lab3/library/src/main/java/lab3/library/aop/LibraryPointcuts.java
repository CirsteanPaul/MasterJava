package lab3.library.aop;

import org.aspectj.lang.annotation.Pointcut;

/** Named pointcuts shared by all library aspects. */
public final class LibraryPointcuts {

    private LibraryPointcuts() {
    }

    @Pointcut("execution(* lab3.library.service.BorrowingService.borrowBook(Long, Long))")
    public static void borrow() {
    }

    @Pointcut("execution(* lab3.library.service.BorrowingService.returnBook(Long, Long))")
    public static void giveBack() {
    }

    @Pointcut("borrow() || giveBack()")
    public static void loanOperation() {
    }
}
