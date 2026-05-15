package com.example.learning.aspect;

import com.example.learning.model.Book;
import com.example.learning.repository.BookRepository;
import com.example.learning.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Optional;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * LESSON: Testing AOP aspects
 *
 * Strategy: load the real Spring context (@SpringBootTest) so the AOP proxy
 * is actually created. Mock the repository so tests don't need a database.
 * Capture log output with a Logback ListAppender and assert on the messages.
 *
 * Why @SpringBootTest and not @ExtendWith(SpringExtension.class)?
 * AOP proxies are only woven when the full Spring ApplicationContext is running.
 * A plain unit test with `new BookService()` bypasses the proxy entirely —
 * your aspect code would never execute.
 */
@SpringBootTest
class AopAspectTest {

    @Autowired
    private BookService bookService;  // this is the PROXY, not the real BookService

    @MockBean
    private BookRepository bookRepository;  // replaced with a Mockito mock

    // Logback appender that captures log messages into a List we can assert on
    private ListAppender<ILoggingEvent> loggingAppender;
    private ListAppender<ILoggingEvent> performanceAppender;
    private ListAppender<ILoggingEvent> auditAppender;

    @BeforeEach
    void setUp() {
        loggingAppender    = attachAppender(LoggingAspect.class);
        performanceAppender = attachAppender(PerformanceAspect.class);
        auditAppender       = attachAppender(AuditAspect.class);

        // Default mock behaviour
        Book savedBook = new Book("Clean Code", "Robert Martin", 35.99);
        // Use reflection-style setter via constructor — id is auto-set by DB normally
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(bookRepository.findAll()).thenReturn(List.of(savedBook));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(savedBook));
    }

    // ── @Before and @AfterReturning (LoggingAspect) ───────────────────────

    @Test
    void loggingAspect_shouldLogBeforeAndAfterReturning_onCreateBook() {
        bookService.createBook(new Book("Clean Code", "Robert Martin", 35.99));

        List<String> messages = getMessages(loggingAppender);

        // @Before fired
        assertThat(messages)
            .anyMatch(m -> m.contains("[AOP @Before]") && m.contains("createBook"));

        // @AfterReturning fired (method returned successfully)
        assertThat(messages)
            .anyMatch(m -> m.contains("[AOP @AfterReturning]") && m.contains("createBook"));

        // @AfterThrowing must NOT fire (no exception)
        assertThat(messages)
            .noneMatch(m -> m.contains("[AOP @AfterThrowing]"));
    }

    @Test
    void loggingAspect_shouldLogAfterThrowing_whenRepositoryThrows() {
        when(bookRepository.save(any(Book.class)))
            .thenThrow(new RuntimeException("DB connection lost"));

        try {
            bookService.createBook(new Book("Bad Book", "Author", 0));
        } catch (RuntimeException ignored) {}

        List<String> messages = getMessages(loggingAppender);

        // @AfterThrowing must fire
        assertThat(messages)
            .anyMatch(m -> m.contains("[AOP @AfterThrowing]") && m.contains("createBook"));

        // @AfterReturning must NOT fire (exception was thrown)
        assertThat(messages)
            .noneMatch(m -> m.contains("[AOP @AfterReturning]") && m.contains("createBook"));
    }

    // ── @Around (PerformanceAspect) ───────────────────────────────────────

    @Test
    void performanceAspect_shouldLogElapsedTime_onAnyServiceMethod() {
        bookService.getAllBooks();

        List<String> messages = getMessages(performanceAppender);

        assertThat(messages)
            .anyMatch(m -> m.contains("[AOP @Around]")
                       && m.contains("getAllBooks")
                       && m.contains("ms"));
    }

    // ── Pointcut selectivity (AuditAspect) ───────────────────────────────

    @Test
    void auditAspect_shouldFire_onWriteOperations() {
        bookService.createBook(new Book("Clean Code", "Robert Martin", 35.99));

        assertThat(getMessages(auditAppender))
            .anyMatch(m -> m.contains("[AUDIT]") && m.contains("createBook"));
    }

    @Test
    void auditAspect_shouldNotFire_onReadOperations() {
        // LESSON: pointcut verification
        // AuditAspect matches only create*/update*/delete* method names.
        // getAllBooks() and getBookById() must NOT trigger the audit advice.
        bookService.getAllBooks();
        bookService.getBookById(1L);

        assertThat(getMessages(auditAppender))
            .noneMatch(m -> m.contains("[AUDIT]"));
    }

    @Test
    void auditAspect_shouldFire_onDeleteOperation() {
        when(bookRepository.existsById(1L)).thenReturn(true);
        bookService.deleteBook(1L);

        assertThat(getMessages(auditAppender))
            .anyMatch(m -> m.contains("[AUDIT]") && m.contains("deleteBook"));
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    /**
     * Attaches a capturing appender to the logger of a given class.
     * This is how you intercept SLF4J/Logback output in tests without
     * mocking the logger or parsing stdout.
     */
    private ListAppender<ILoggingEvent> attachAppender(Class<?> loggerClass) {
        Logger logger = (Logger) LoggerFactory.getLogger(loggerClass);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        return appender;
    }

    private List<String> getMessages(ListAppender<ILoggingEvent> appender) {
        return appender.list.stream()
            .map(ILoggingEvent::getFormattedMessage)
            .toList();
    }
}
