package com.nvminh162.employee.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
    info = @Info(
        title = "Employee API Specification - @nvminh162",
        description = "Api docs for employee service",
        version = "1.0",
        contact = @Contact(
            name = "@nvminh162",
            email = "nvminh162@gmail.com",
            url = "nvminh162.com"
        ),
        license = @License(
            name = "MIT License",
            url = "nvminh162.com/license"
        ),
        termsOfService = "nvminh162.com/terms"
    ),
    servers = {
        @Server(
            description = "local ENV",
            url = "http://localhost:9002"
        ),
        @Server(
            description = "Dev ENV",
            url = "https://nvminh162.dev.com/"
        ),
        @Server(
            description = "Prod ENV",
            url = "https://nvminh162.prod.com/"
        )
    }
)
public class OpenAPIConfig {

}
