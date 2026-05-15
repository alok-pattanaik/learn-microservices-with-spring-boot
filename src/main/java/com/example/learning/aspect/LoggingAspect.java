package com.example.learning.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * LESSON: @Aspect
 * Marks this class as an Aspect — a module of cross-cutting concern logic.
 * It still needs @Component so Spring registers it as a bean.
 *
 * Without AOP, every service method would need to call a logger manually.
 * With this aspect, logging is applied to ALL service methods automatically,
 * with zero changes to BookService.
 *
 * LESSON: Advice types demonstrated here:
 *   @Before        — runs BEFORE the method executes
 *   @AfterReturning — runs AFTER the method returns successfully
 *   @AfterThrowing  — runs ONLY if the method throws an exception
 */
@Aspect
@Component
public class      LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * LESSON: @Pointcut
     * Defines a reusable expression that selects which methods to intercept.
     * This one matches: any method (*), any return type (..), in any class
     * inside the service package, with any arguments (..).
     *
     * Pointcut expression anatomy:
     *   execution(* com.example.learning.service.*.*(..))
     *              |  |_________________________| |  ||
     *              |  package + class             |  any args
     *              any return type               any method name
     */
    @Pointcut("execution(* com.example.learning.service.*.*(..))")
    public void serviceLayer() {}

    /**
     * LESSON: @Before
     * Runs before the matched method. Receives a JoinPoint which gives
     * access to the method name, class, and arguments.
     *
     * JoinPoint.getSignature() → method signature
     * JoinPoint.getArgs()      → the actual argument values passed in
     */
    @Before("serviceLayer()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("[AOP @Before] Calling: {}.{}() with args: {}",
            joinPoint.getTarget().getClass().getSimpleName(),
            joinPoint.getSignature().getName(),
            Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * LESSON: @AfterReturning
     * Runs after the method returns successfully.
     * The 'returning' attribute binds the return value to a parameter.
     * If the method throws, this advice does NOT run.
     */
    @AfterReturning(pointcut = "serviceLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("[AOP @AfterReturning] {}.{}() returned: {}",
            joinPoint.getTarget().getClass().getSimpleName(),
            joinPoint.getSignature().getName(),
            result);
    }

    /**
     * LESSON: @AfterThrowing
     * Runs ONLY when the method throws an exception.
     * The 'throwing' attribute binds the exception to a parameter.
     * This is the right place to log errors centrally — no try/catch needed
     * in every service method.
     */
    @AfterThrowing(pointcut = "serviceLayer()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Exception ex) {
        log.error("[AOP @AfterThrowing] {}.{}() threw: {}",
            joinPoint.getTarget().getClass().getSimpleName(),
            joinPoint.getSignature().getName(),
            ex.getMessage());
    }
}
