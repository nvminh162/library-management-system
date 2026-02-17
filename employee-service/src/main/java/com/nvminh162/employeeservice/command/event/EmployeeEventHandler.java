package com.nvminh162.employeeservice.command.event;

import java.util.Optional;

import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.nvminh162.employeeservice.command.data.Employee;
import com.nvminh162.employeeservice.command.data.EmployeeRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class EmployeeEventHandler {

    EmployeeRepository employeeRepository;

    @EventHandler
    public void on(EmployeeCreatedEvent event) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(event, employee);
        employeeRepository.save(employee);
    }

    @EventHandler
    public void on(EmployeeUpdatedEvent event) throws Exception{
        Optional<Employee> oldEmployee = employeeRepository.findById(event.getId());
        Employee employee = oldEmployee.orElseThrow(() -> new Exception("Employee not found"));
        employee.setFirstName(event.getFirstName());
        employee.setKin(event.getKin());
        employee.setLastName(event.getLastName());
        employee.setIsDisciplined(event.getIsDisciplined());
        employeeRepository.save(employee);
    }

    @EventHandler
    public void on(EmployeeDeletedEvent event) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(event.getId());
        optionalEmployee.ifPresent(employeeRepository::delete);
    }
}
