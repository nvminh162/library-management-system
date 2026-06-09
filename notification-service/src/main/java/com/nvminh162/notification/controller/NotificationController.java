package com.nvminh162.notification.controller;

import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import com.nvminh162.notification.service.EmailService;
import com.nvminh162.notification.model.EmployeeResponseModel;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationController {
    
    final CircuitBreakerFactory<?, ?> circuitBreakerFactory;
    final WebClient.Builder webClientBuilder;
    final EmailService emailService;

    @Value("${app.employee-service-url}")
    String employeeServiceUrl;

    @Value("${app.notification-email}")
    String notificationEmail;
    
    @GetMapping("/employees/{employeeId}")
    public EmployeeResponseModel getEmployee(@PathVariable String employeeId) {
        EmployeeResponseModel model = circuitBreakerFactory.create("getEmployee").run(
                () -> webClientBuilder.build()
                        .get()
                        .uri(employeeServiceUrl + "/api/v1/employees/" + employeeId)
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
            emailService.sendEmail(notificationEmail, "Employee Disciplined", body, true, null);
        }
        return model;
    }
}
