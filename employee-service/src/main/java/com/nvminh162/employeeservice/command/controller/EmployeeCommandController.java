package com.nvminh162.employeeservice.command.controller;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nvminh162.employeeservice.command.command.CreateEmployeeCommand;
import com.nvminh162.employeeservice.command.command.UpdateEmployeeCommand;
import com.nvminh162.employeeservice.command.model.CreateEmployeeModel;
import com.nvminh162.employeeservice.command.model.UpdateEmployeeModel;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
@RequestMapping("/api/v1/employees")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EmployeeCommandController {

    CommandGateway commandGateway;

    @PostMapping
    public String addEmployee(@RequestBody CreateEmployeeModel model) {
        CreateEmployeeCommand command = CreateEmployeeCommand.builder()
            .id(UUID.randomUUID().toString())
            .firstName(model.getFirstName())
            .lastName(model.getLastName())
            .kin(model.getKin())
            .isDisciplined(false)
            .build();
        return commandGateway.sendAndWait(command);
    }

    @PutMapping("/{employeeId}")
    public String updateEmployee(@RequestBody UpdateEmployeeModel model, @PathVariable String employeeId) {
        UpdateEmployeeCommand command = UpdateEmployeeCommand.builder()
            .id(employeeId)
            .firstName(model.getFirstName())
            .lastName(model.getLastName())
            .kin(model.getKin())
            .isDisciplined(model.getIsDisciplined())
            .build();
        return commandGateway.sendAndWait(command);
    }
}
