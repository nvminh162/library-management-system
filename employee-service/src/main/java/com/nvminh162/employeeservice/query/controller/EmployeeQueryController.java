package com.nvminh162.employeeservice.query.controller;

import java.util.List;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.RestController;

import com.nvminh162.employeeservice.query.model.EmployeeResponseModel;
import com.nvminh162.employeeservice.query.queries.GetAllEmployeeQuery;
import com.nvminh162.employeeservice.query.queries.GetDetailEmployeeQuery;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee Query")
public class EmployeeQueryController {

    QueryGateway queryGateway;

    @Operation(
        summary = "Get list employee",
        description = "Get endpoint for employee with filter",
        responses = {
            @ApiResponse(
                description = "Success",
                responseCode = "200"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized / Invalid token"
            )
        }
    )
    @GetMapping
    public List<EmployeeResponseModel> getAllEmployees(
            @RequestParam(required = false, defaultValue = "false") Boolean isDisciplined) {
        return queryGateway.query(new GetAllEmployeeQuery(isDisciplined),
                ResponseTypes.multipleInstancesOf(EmployeeResponseModel.class)).join();
    }

    @GetMapping("/{employeeId}")
    public EmployeeResponseModel getDetailEmployee(@PathVariable String employeeId) {
        GetDetailEmployeeQuery query = GetDetailEmployeeQuery.builder().id(employeeId).build();
        return queryGateway.query(query, ResponseTypes.instanceOf(EmployeeResponseModel.class)).join();
    }
}
