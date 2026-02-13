package com.nvminh162.bookservice.command.controller;

import com.nvminh162.bookservice.command.command.CreateBookCommand;
import com.nvminh162.bookservice.command.command.DeleteBookCommand;
import com.nvminh162.bookservice.command.command.UpdateBookCommand;
import com.nvminh162.bookservice.command.model.BookRequestModel;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookCommandController {

    CommandGateway commandGateway;

    @PostMapping
    public String addBook(@RequestBody BookRequestModel model) {
        CreateBookCommand command = CreateBookCommand.builder()
                .id(UUID.randomUUID().toString())
                .name(model.getName())
                .author(model.getAuthor())
                .isReady(true)
                .build();
        return commandGateway.sendAndWait(command);
    }

    @PutMapping("/{bookId}")
    public String updateBook(@RequestBody BookRequestModel model, @PathVariable String bookId) {
        UpdateBookCommand command = UpdateBookCommand.builder()
                .id(bookId)
                .name(model.getName())
                .author(model.getAuthor())
                .isReady(model.getIsReady())
                .build();
        return commandGateway.sendAndWait(command);
    }

    @DeleteMapping("/{bookId}")
    public String deleteBook(@PathVariable String bookId) {
        DeleteBookCommand command = DeleteBookCommand.builder()
                .id(bookId)
                .build();
        return commandGateway.sendAndWait(command);
    }
}
