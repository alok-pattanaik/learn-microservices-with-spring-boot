package com.example.learning.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * LESSON: @Around — the most powerful advice type
 *
 * @Around completely wraps the method execution.
 * Unlike @Before/@After, it receives a ProceedingJoinPoint and YOU decide
 * when (or even whether) to call proceed() to execute the real method.
 *
 * This makes @Around capable of:
 *   - measuring execution time (this example)
 *   - caching results (call proceed() only on cache miss)
 *   - retrying on failure (call proceed() again in a catch block)
 *   - rate-limiting (don't call proceed() if limit exceeded)
 *
 * The key difference from @Before + @After:
 *   @Before + @After = two separate advice methods, cannot share local state
 *   @Around           = one method with a local variable that survives proceed()
 *
 * That's why performance timing requires @Around — you need startTime before
 * and endTime after, in the same stack frame.
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

    /**
     * Intercepts all service methods and logs how long each one takes.
     *
     * ProceedingJoinPoint extends JoinPoint — it adds proceed() which
     * actually calls the real method and returns its result.
     *
     * Note: @Around must either call proceed() or throw.
     * If you forget proceed(), the real method never executes.
     */
    @Around("execution(* com.example.learning.service.*.*(..))")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        try {
            // Call the real method — this is where BookService actually runs
            Object result = joinPoint.proceed();

            long elapsed = System.currentTimeMillis() - start;
            log.info("[AOP @Around] {}.{}() completed in {} ms",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                elapsed);

            return result; // must return the result back to the caller
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.warn("[AOP @Around] {}.{}() FAILED after {} ms",
                joinPoint.getTarget().getClass().getSimpleName(),
                joinPoint.getSignature().getName(),
                elapsed);
            throw ex; // must re-throw so @Transactional rollback still works
        }
    }
}
