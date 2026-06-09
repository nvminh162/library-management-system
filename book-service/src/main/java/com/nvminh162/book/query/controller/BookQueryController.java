package com.nvminh162.book.query.controller;

import com.nvminh162.contract.model.BookResponseCommonModel;
import com.nvminh162.book.query.queries.GetAllBookQuery;
import com.nvminh162.contract.queries.GetBookDetailQuery;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class BookQueryController {

    QueryGateway queryGateway;

    @GetMapping
    public List<BookResponseCommonModel> getAllBooks() {
        GetAllBookQuery query = new GetAllBookQuery();
        List<BookResponseCommonModel> books = queryGateway.query(query, ResponseTypes.multipleInstancesOf(BookResponseCommonModel.class)).join();
        log.info(">>> GET ALL BOOKS: {}", books.toString());
        return books;
    }

    @GetMapping("/{bookId}")
    public BookResponseCommonModel getDetail(@PathVariable String bookId) {
        GetBookDetailQuery query = new GetBookDetailQuery(bookId);
        BookResponseCommonModel book = queryGateway.query(query, ResponseTypes.instanceOf(BookResponseCommonModel.class)).join();
        return book;
    }
}
