package com.example.learning.repository;

import com.example.learning.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * LESSON: @Repository — more than just a marker
 *
 * @Repository does two things:
 *   1. Marks this interface as a Spring-managed bean (like @Component).
 *   2. Enables automatic exception translation — any low-level persistence
 *      exception (e.g. SQLException, HibernateException) is caught and
 *      re-thrown as a Spring DataAccessException.
 *
 * This matters because:
 *   - Your service layer never has to deal with database-specific exceptions.
 *   - You can swap H2 for PostgreSQL without changing exception handling code.
 *   - @Component does NOT provide this translation; @Repository does.
 *
 * JpaRepository<Book, Long> gives us save(), findById(), findAll(),
 * delete() etc. for free — no SQL needed.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Spring Data derives the SQL from the method name automatically.
    // "find books where author = ?" → no implementation needed.
    List<Book> findByAuthor(String author);

    // "find books where title contains ?" — case-insensitive search
    List<Book> findByTitleContainingIgnoreCase(String keyword);
}
