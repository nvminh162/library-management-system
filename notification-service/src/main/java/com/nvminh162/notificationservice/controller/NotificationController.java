package com.nvminh162.notificationservice.controller;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.nvminh162.commonservice.service.EmailService;
import com.nvminh162.notificationservice.model.EmployeeResponseModel;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    
    CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    WebClient.Builder webClientBuilder;
    EmailService emailService;
    
    @GetMapping("/employees/{employeeId}")
    public EmployeeResponseModel getEmployee(@PathVariable String employeeId) {
        EmployeeResponseModel model = circuitBreakerFactory.create("getEmployee").run(
                () -> webClientBuilder.build()
                        .get()
                        .uri("http://localhost:9002/api/v1/employees/" + employeeId)
                        .retrieve()
                        .bodyToMono(EmployeeResponseModel.class)
                        .block(),
                t -> {
                    EmployeeResponseModel fallback = new EmployeeResponseModel();
                    fallback.setFirstName("Anonymous");
                    fallback.setLastName("Employee");
                    return fallback;
                }
        );
        if (model != null) {
            String body = "Employee " + model.getFirstName() + " " + model.getLastName() + " has been disciplined.";
            emailService.sendEmail("nvminh162@gmail.com", "Employee Disciplined", body, true, null);
        }
        return model;
    }
}
