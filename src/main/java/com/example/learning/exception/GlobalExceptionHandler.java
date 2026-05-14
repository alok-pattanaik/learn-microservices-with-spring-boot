package com.example.learning.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

/**
 * LESSON: @RestControllerAdvice + @ExceptionHandler
 *
 * @RestControllerAdvice is @ControllerAdvice + @ResponseBody.
 * It applies globally to all controllers — you don't repeat exception
 * handling logic in every controller method.
 *
 * @ExceptionHandler(SomeException.class) catches that exception type
 * wherever it is thrown from any controller in the application.
 *
 * -----------------------------------------------------------------------
 * LESSON: DataAccessException — why @Repository matters
 *
 * @Repository automatically translates low-level persistence exceptions
 * (SQLException, HibernateException, etc.) into Spring's
 * DataAccessException hierarchy.
 *
 * This handler catches DataAccessException, meaning the service and
 * controller layers never need to know which database is running underneath.
 * Swap H2 for PostgreSQL → this handler still works, unchanged.
 *
 * If BookRepository were annotated with @Component instead of @Repository,
 * the translation would NOT happen and raw JPA/JDBC exceptions would
 * propagate up, breaking this handler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles any database-level error translated by @Repository.
     * The caller gets a clean 500 JSON response, never a stack trace.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, Object>> handleDataAccessException(
            DataAccessException ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Database error",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()
                ));
    }

    /**
     * Catch-all for any other unexpected RuntimeException.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "Unexpected error",
                        "message", ex.getMessage(),
                        "timestamp", Instant.now().toString()
                ));
    }
}
