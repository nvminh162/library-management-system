package com.nvminh162.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nvminh162.userservice.dto.keycloak.LoginRequest;
import com.nvminh162.userservice.dto.keycloak.UserTokenExchangeResponse;
import com.nvminh162.userservice.service.IUserService;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("api/v1/public")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PublicController {

    IUserService userService;

    @PostMapping("/login")
    public ResponseEntity<UserTokenExchangeResponse> login(@RequestBody LoginRequest request){
        return ResponseEntity.ok(userService.login(request));
    }
}
