package com.nvminh162.employeeservice.query.projection;

import java.util.List;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.nvminh162.employeeservice.command.data.EmployeeRepository;
import com.nvminh162.employeeservice.query.model.EmployeeResponseModel;
import com.nvminh162.employeeservice.query.queries.GetAllEmployeeQuery;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EmployeeProjection {

    EmployeeRepository employeeRepository;

    @QueryHandler
    public List<EmployeeResponseModel> handle(GetAllEmployeeQuery query) {
        return employeeRepository.findAll().stream().map(employee -> {
            EmployeeResponseModel model = new EmployeeResponseModel();
            BeanUtils.copyProperties(employee, model);
            return model;
        }).toList();
    }
}
