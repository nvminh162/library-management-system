package com.nvminh162.employee.query.projection;

import java.util.List;

import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.nvminh162.contract.model.EmployeeResponseCommonModel;
import com.nvminh162.contract.queries.GetDetailEmployeeQuery;
import com.nvminh162.employee.command.data.Employee;
import com.nvminh162.employee.command.data.EmployeeRepository;
import com.nvminh162.employee.query.queries.GetAllEmployeeQuery;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EmployeeProjection {

    EmployeeRepository employeeRepository;

    @QueryHandler
    public List<EmployeeResponseCommonModel> handle(GetAllEmployeeQuery query) {
        return employeeRepository.findByIsDisciplined(query.getIsDisciplined()).stream().map(employee -> {
            EmployeeResponseCommonModel model = new EmployeeResponseCommonModel();
            BeanUtils.copyProperties(employee, model);
            return model;
        }).toList();
    }

    @QueryHandler
    public EmployeeResponseCommonModel handle(GetDetailEmployeeQuery query) throws Exception {
        EmployeeResponseCommonModel model = new EmployeeResponseCommonModel();
        Employee employee = employeeRepository.findById(query.getId())
                .orElseThrow(() -> new Exception("Employee ID not found " + query.getId()));
        BeanUtils.copyProperties(employee, model);
        return model;
    }
}
