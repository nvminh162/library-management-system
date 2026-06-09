package com.nvminh162.book.command.event;

import com.nvminh162.book.command.data.Book;
import com.nvminh162.book.command.data.BookRepository;
import com.nvminh162.contract.event.BookUpdatedStatusEvent;
import com.nvminh162.contract.event.BookRollBackStatusEvent;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
public class BookEventsHandler {

    BookRepository bookRepository;

    @EventHandler
    public void on(BookCreatedEvent event) {
        Book book = new Book();
        BeanUtils.copyProperties(event, book);
        bookRepository.save(book);
    }

    @EventHandler
    public void on(BookUpdatedEvent event) {
        Optional<Book> optionalBook = bookRepository.findById(event.getId());
        optionalBook.ifPresent(book -> {
            book.setName(event.getName());
            book.setAuthor(event.getAuthor());
            book.setIsReady(event.getIsReady());
            bookRepository.save(book);
        });
    }

    @EventHandler
    public void on(BookDeletedEvent event) {
        Optional<Book> optionalBook = bookRepository.findById(event.getId());
        optionalBook.ifPresent(bookRepository::delete);
    }

    @EventHandler
    public void on(BookUpdatedStatusEvent event) {
        Optional<Book> optionalBook = bookRepository.findById(event.getBookId());
        optionalBook.ifPresent(book -> {
            book.setIsReady(event.getIsReady());
            bookRepository.save(book);
        });
    }

    @EventHandler
    public void on(BookRollBackStatusEvent event) {
        Optional<Book> optionalBook = bookRepository.findById(event.getBookId());
        optionalBook.ifPresent(book -> {
            book.setIsReady(event.getIsReady());
            bookRepository.save(book);
        });
    }
}
