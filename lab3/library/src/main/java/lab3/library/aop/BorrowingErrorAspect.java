package lab3.library.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Intercepts every exception thrown by a borrowing operation (including the ones raised by
 * {@link BorrowingValidationAspect}, which runs inside it) and logs the failed operation and message.
 */
@Aspect
@Component
@Order(2)
public class BorrowingErrorAspect {

    private static final Logger log = LoggerFactory.getLogger(BorrowingErrorAspect.class);

    @AfterThrowing(pointcut = "lab3.library.aop.LibraryPointcuts.loanOperation()", throwing = "ex")
    public void logFailure(JoinPoint joinPoint, Exception ex) {
        log.error("[ERROR] operation {}{} failed with {}: {}",
                joinPoint.getSignature().toShortString().replace("(..)", ""),
                Arrays.toString(joinPoint.getArgs()), ex.getClass().getSimpleName(), ex.getMessage());
    }
}
