package com.example.learning.controller;

import com.example.learning.model.Book;
import com.example.learning.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * LESSON: @RestController
 * Combines @Controller + @ResponseBody.
 *   - @Controller      → marks this as a web-layer bean (stereotype annotation)
 *   - @ResponseBody    → every method return value is written directly to the
 *                        HTTP response body as JSON (via Jackson), instead of
 *                        being treated as a view name.
 *
     * If you used plain @Controller you would need @ResponseBody on every method,
 * or you'd have to return a ModelAndView. @RestController saves that boilerplate.
 *
 * LESSON: @RequestMapping at class level
 * Sets the base URL prefix for all endpoints in this controller.
 * Every method below is relative to /api/books.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * LESSON: @GetMapping
     * Shorthand for @RequestMapping(method = RequestMethod.GET).
     * GET is idempotent — calling it multiple times has no side effects.
     *
     * GET /api/books
     * GET /api/books?author=Tolkien            ← @RequestParam (optional)
     * GET /api/books?title=ring                ← @RequestParam (optional)
     *
     * LESSON: @RequestParam
     * Extracts a query parameter from the URL (?key=value).
     * required = false means the endpoint works even without the param.
     */
    @GetMapping
    public List<Book> getAllBooks(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String title) {

        if (author != null) return bookService.getBooksByAuthor(author);
        if (title  != null) return bookService.searchByTitle(title);
        return bookService.getAllBooks();
    }

    /**
     * LESSON: @PathVariable
     * Extracts a value embedded IN the URL path (not a query parameter).
     *
     * GET /api/books/1   → id = 1
     * GET /api/books/42  → id = 42
     *
     * Compare with @RequestParam:
     *   @PathVariable  → /api/books/1          (part of the path)
     *   @RequestParam  → /api/books?id=1       (after the ?)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {
        return findBookById(id);
    }

    @GetMapping(params = "id")
    public ResponseEntity<Book> getBookByRequestParam(@RequestParam Long id) {
        return findBookById(id);
    }

    private ResponseEntity<Book> findBookById(Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * LESSON: @PostMapping + @RequestBody
     * POST is used for creating a new resource.
     *
     * @RequestBody deserialises the JSON request payload into a Book object.
     * Jackson does this automatically — Spring is configured with it by default.
     *
     * Example request body:
     * {
     *   "title": "The Hobbit",
     *   "author": "Tolkien",
     *   "price": 12.99
     * }
     *
     * Compare the three param annotations:
     *   @RequestParam  → query string  (?key=value)
     *   @PathVariable  → URL segment   (/resource/{id})
     *   @RequestBody   → request body  (JSON payload)
     */
    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody Book book) {
        Book created = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * LESSON: @PutMapping
     * PUT replaces a resource entirely. It is idempotent — calling it
     * multiple times with the same body produces the same result.
     * Uses both @PathVariable (which book?) and @RequestBody (new data).
     */
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
            @PathVariable Long id,
            @RequestBody Book updatedBook) {

        return bookService.updateBook(id, updatedBook)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * LESSON: @DeleteMapping
     * DELETE is also idempotent — deleting something that's already gone
     * is still a valid outcome.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (bookService.deleteBook(id)) {
            return ResponseEntity.noContent().build();   // 204
        }
        return ResponseEntity.notFound().build();        // 404
    }
}
