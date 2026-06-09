package com.nvminh162.book.query.projection;

import com.nvminh162.book.command.data.Book;
import com.nvminh162.book.command.data.BookRepository;
import com.nvminh162.contract.model.BookResponseCommonModel;
import com.nvminh162.book.query.queries.GetAllBookQuery;
import com.nvminh162.contract.queries.GetBookDetailQuery;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookProjection {

    BookRepository bookRepository;

    @QueryHandler
    public List<BookResponseCommonModel> handle(GetAllBookQuery query) {
        /*
        List<Book> books = bookRepository.findAll();
        List<BookResponseCommonModel> booksResponse = new ArrayList<>();
        books.forEach(book -> {
            BookResponseCommonModel model = new BookResponseCommonModel();
            BeanUtils.copyProperties(book, model);
            booksResponse.add(model);
        });
        return booksResponse;
        */

        List<Book> books = bookRepository.findAll();
        return books.stream().map(book -> {
            BookResponseCommonModel model = new BookResponseCommonModel();
            BeanUtils.copyProperties(book, model);
            return model;
        }).toList();
    }

    @QueryHandler
    public BookResponseCommonModel handle(GetBookDetailQuery query) throws Exception {
        BookResponseCommonModel model = new BookResponseCommonModel();

        Book book = bookRepository.findById(query.getId()).orElseThrow(() -> new Exception("Book ID not found " + query.getId()));

        BeanUtils.copyProperties(book, model);
        return model;
    }
}
