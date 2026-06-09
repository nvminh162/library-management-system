package com.nvminh162.employee.command.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateEmployeeModel {

    @NotBlank(message = "First name is mandatory")
    String firstName;
    @NotBlank(message = "Last name is mandatory")
    String lastName;
    @NotBlank(message = "Kin is mandatory")
    String kin;
    @NotNull(message = "isDisciplined is mandatory")
    Boolean isDisciplined;
}
