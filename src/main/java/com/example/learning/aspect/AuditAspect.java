package com.example.learning.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.learning.model.Book;

/**
 * LESSON: Narrower pointcut expressions
 *
 * The LoggingAspect matched ALL service methods with a broad wildcard.
 * This aspect shows how to write targeted pointcuts for specific operations.
 *
 * Real-world use: audit trails need to record WHO changed WHAT and WHEN.
 * Without AOP you'd add audit code inside createBook(), updateBook(), deleteBook()
 * — three places to maintain, easy to forget. With AOP it's declared once here.
 *
 * LESSON: Pointcut operators
 *   &&   AND — both expressions must match
 *   ||   OR  — either expression matches
 *   !    NOT — expression must not match
 *
 * This pointcut matches methods whose name starts with "create" OR "update" OR "delete",
 * inside the service package. Read-only methods (getBook, getAllBooks) are excluded.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    /**
     * Matches only mutating service methods by name prefix.
     *
     * execution syntax:
     *   execution(* com.example.learning.service.*.create*(..))
     *                                              |_____|
     *                                              name starts with "create"
     */
    @Pointcut("execution(* com.example.learning.service.*.create*(..))" +
              " || execution(* com.example.learning.service.*.update*(..))" +
              " || execution(* com.example.learning.service.*.delete*(..))")
    public void writeOperations() {}

    /**
     * Runs after a write operation completes successfully.
     * In production you'd persist an AuditLog entity to the database.
     *
     * LESSON: @AfterReturning on a narrower pointcut
     * This only fires on create/update/delete — not on getAllBooks() or getBookById().
     * The pointcut expression is the filter; the advice is the action.
     */
    @AfterReturning(pointcut = "writeOperations()", returning = "result")
    public void auditWrite(JoinPoint joinPoint, Object result) {
        String operation = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        // In production: save an AuditLog record to DB with timestamp + user
        if (result instanceof Book book) {
            log.info("[AUDIT] operation={} bookId={} title=\"{}\"",
                operation, book.getId(), book.getTitle());
        } else {
            // deleteBook returns boolean, not a Book
            log.info("[AUDIT] operation={} args={}",
                operation, java.util.Arrays.toString(args));
        }
    }
}
