package com.nvminh162.userservice.repository;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.nvminh162.userservice.dto.keycloak.TokenExchangeParam;
import com.nvminh162.userservice.dto.keycloak.TokenExchangeResponse;
import com.nvminh162.userservice.dto.keycloak.UserCreationParam;

import feign.QueryMap;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@FeignClient(name = "keycloak-client", url = "${keycloak.url}")
public interface KeycloakClient {
    
    @PostMapping(value = "/realms/{realm-name}/protocol/openid-connect/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TokenExchangeResponse exchangeToken(@PathVariable("realm-name") String realm, @QueryMap TokenExchangeParam param);

    @PostMapping(value = "/admin/realms/{realm-name}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> createUser(@PathVariable("realm-name") String realm, @RequestHeader("authorization") String token, @RequestBody UserCreationParam param);
}
