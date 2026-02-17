package com.nvminh162.employeeservice.query.controller;

import java.util.List;

import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.web.bind.annotation.RestController;

import com.nvminh162.employeeservice.query.model.EmployeeResponseModel;
import com.nvminh162.employeeservice.query.queries.GetAllEmployeeQuery;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/api/v1/employees")
public class EmployeeQueryController {

    QueryGateway queryGateway;

    @GetMapping
    public List<EmployeeResponseModel> getAllEmployees() {
        GetAllEmployeeQuery query = new GetAllEmployeeQuery();
        return queryGateway.query(query, ResponseTypes.multipleInstancesOf(EmployeeResponseModel.class)).join();
        
    }
    
}
