package lab3.library.service;

import lab3.library.domain.Book;
import lab3.library.exception.BookNotFoundException;
import lab3.library.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository books;

    public BookService(BookRepository books) {
        this.books = books;
    }

    public Book add(Book book) {
        return books.save(book);
    }

    public Book get(Long id) {
        return books.findById(id).orElseThrow(() -> new BookNotFoundException("book #" + id + " does not exist"));
    }

    public List<Book> available() {
        return books.findAll().stream().filter(Book::isAvailable).toList();
    }
}
