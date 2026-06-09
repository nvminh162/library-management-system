package com.nvminh162.borrow.command.controller;

import java.util.Date;
import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nvminh162.borrow.command.command.CreateBorrowingCommand;
import com.nvminh162.borrow.command.model.BorrowingCreateModel;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/v1/borrowing")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BorrowingCommandController {

    CommandGateway commandGateway;

    @PostMapping
    public String createBorrowing(@RequestBody BorrowingCreateModel model) {
        CreateBorrowingCommand command = CreateBorrowingCommand.builder()
                .id(UUID.randomUUID().toString())
                .bookId(model.getBookId())
                .employeeId(model.getEmployeeId())
                .borrowingDate(new Date())
                .build();
        return commandGateway.sendAndWait(command);
    }

}
