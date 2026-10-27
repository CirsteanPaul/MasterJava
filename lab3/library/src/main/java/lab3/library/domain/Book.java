package lab3.library.domain;

import java.util.Objects;

public class Book {

    private final Long id;
    private final String title;
    private final String author;
    private final BookType type;
    private volatile boolean available = true;

    public Book(Long id, String title, String author, BookType type) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.author = author;
        this.type = Objects.requireNonNull(type);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public BookType getType() { return type; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "'%s' by %s (%s)".formatted(title, author, type);
    }
}
