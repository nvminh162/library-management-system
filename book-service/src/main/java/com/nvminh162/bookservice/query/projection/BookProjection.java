package com.nvminh162.bookservice.query.projection;

import com.nvminh162.bookservice.command.data.Book;
import com.nvminh162.bookservice.command.data.BookRepository;
import com.nvminh162.bookservice.query.model.BookResponseModel;
import com.nvminh162.bookservice.query.queries.GetAllBookQuery;
import com.nvminh162.bookservice.query.queries.GetBookDetailQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookProjection {

    BookRepository bookRepository;

    @QueryHandler
    public List<BookResponseModel> handle(GetAllBookQuery query) {
        /*
        List<Book> books = bookRepository.findAll();
        List<BookResponseModel> booksResponse = new ArrayList<>();
        books.forEach(book -> {
            BookResponseModel model = new BookResponseModel();
            BeanUtils.copyProperties(book, model);
            booksResponse.add(model);
        });
        return booksResponse;
        */

        List<Book> books = bookRepository.findAll();
        return books.stream().map(book -> {
            BookResponseModel model = new BookResponseModel();
            BeanUtils.copyProperties(book, model);
            return model;
        }).toList();
    }

    @QueryHandler
    public BookResponseModel handle(GetBookDetailQuery query) throws Exception {
        BookResponseModel model = new BookResponseModel();

        Book book = bookRepository.findById(query.getId()).orElseThrow(() -> new Exception("Book ID not found " + query.getId()));

        BeanUtils.copyProperties(book, model);
        return model;
    }
}
