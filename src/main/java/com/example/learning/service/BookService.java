package com.example.learning.service;

import com.example.learning.model.Book;
import com.example.learning.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * LESSON: @Service — stereotype annotation for business logic
 *
 * Stereotype annotations (@Component, @Service, @Repository, @Controller)
 * all register the class as a Spring bean via component scanning.
 * They are functionally similar but convey INTENT:
 *
 *   @Component   → generic; no specific role
 *   @Service     → business logic layer  ← we are here
 *   @Repository  → data access layer
 *   @Controller  → web / presentation layer
 *
 * Using @Service here (instead of @Component) signals to every developer
 * reading this code that business rules live here — not in the controller,
 * not in the repository.
 *
 * -----------------------------------------------------------------------
 * LESSON: @Transactional
 * Wrapping a method in @Transactional means:
 *   - A DB transaction is opened before the method runs.
 *   - If the method completes normally → transaction commits.
 *   - If a RuntimeException is thrown → transaction rolls back automatically.
 * This prevents partial writes (e.g. only half of a multi-step save succeeding).
 */
@Service
public class BookService {

    // LESSON: Constructor injection (preferred over @Autowired on a field)
    // Spring sees one constructor → injects the dependency automatically.
    // No @Autowired annotation is needed on a single constructor in Spring 4.3+.
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    public List<Book> getBooksByAuthor(String author) {
        return bookRepository.findByAuthor(author);
    }

    public List<Book> searchByTitle(String keyword) {
        return bookRepository.findByTitleContainingIgnoreCase(keyword);
    }

    @Transactional  // rolls back if anything goes wrong during save
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public Optional<Book> updateBook(Long id, Book updated) {
        return bookRepository.findById(id).map(existing -> {
            existing.setTitle(updated.getTitle());
            existing.setAuthor(updated.getAuthor());
            existing.setPrice(updated.getPrice());
            return bookRepository.save(existing);
        });
    }

    @Transactional
    public boolean deleteBook(Long id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
