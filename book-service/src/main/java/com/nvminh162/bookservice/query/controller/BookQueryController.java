package com.nvminh162.bookservice.query.controller;

import com.nvminh162.bookservice.query.model.BookResponseModel;
import com.nvminh162.bookservice.query.queries.GetAllBookQuery;
import com.nvminh162.bookservice.query.queries.GetBookDetailQuery;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BookQueryController {

    QueryGateway queryGateway;

    @GetMapping
    public List<BookResponseModel> getAllBooks() {
        GetAllBookQuery query = new GetAllBookQuery();
        List<BookResponseModel> books = queryGateway.query(query, ResponseTypes.multipleInstancesOf(BookResponseModel.class)).join();
        return books;
    }

    @GetMapping("/{bookId}")
    public BookResponseModel getDetail(@PathVariable String bookId) {
        GetBookDetailQuery query = new GetBookDetailQuery(bookId);
        BookResponseModel book = queryGateway.query(query, ResponseTypes.instanceOf(BookResponseModel.class)).join();
        return book;
    }
}
