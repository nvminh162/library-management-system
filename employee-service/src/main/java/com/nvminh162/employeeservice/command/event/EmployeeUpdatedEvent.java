package com.nvminh162.employeeservice.command.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeUpdatedEvent {
    String id;
    String firstName;
    String lastName;
    String kin;
    Boolean isDisciplined;
}
