package com.example.learning.model;

import jakarta.persistence.*;

/**
 * LESSON: JPA / Data annotations
 *
 * @Entity  → marks this class as a JPA entity (maps to a database table)
 * @Table   → optional; specifies the table name (defaults to class name)
 * @Id     → marks the primary key field
 * @GeneratedValue → tells JPA to auto-generate the ID value
 * @Column  → optional; customise column name, nullable, length etc.
 */
@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column
    private double price;

    // Constructors
    public Book() {}

    public Book(String title, String author, double price) {
        this.title = title;
        this.author = author;
        this.price = price;
    }

    // Getters & setters
    public Long getId()                   { return id; }
    public String getTitle()              { return title; }
    public void setTitle(String title)    { this.title = title; }
    public String getAuthor()             { return author; }
    public void setAuthor(String author)  { this.author = author; }
    public double getPrice()              { return price; }
    public void setPrice(double price)    { this.price = price; }
}
