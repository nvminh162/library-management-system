package com.nvminh162.userservice.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.nvminh162.userservice.dto.keycloak.Credential;
import com.nvminh162.userservice.dto.keycloak.LoginRequest;
import com.nvminh162.userservice.dto.keycloak.TokenExchangeParam;
import com.nvminh162.userservice.dto.keycloak.TokenExchangeResponse;
import com.nvminh162.userservice.dto.keycloak.UserCreationParam;
import com.nvminh162.userservice.dto.keycloak.UserTokenExchangeParam;
import com.nvminh162.userservice.dto.keycloak.UserTokenExchangeResponse;
import com.nvminh162.userservice.dto.request.UserCreationRequest;
import com.nvminh162.userservice.dto.request.UserUpdatenRequest;
import com.nvminh162.userservice.dto.response.UserResponse;
import com.nvminh162.userservice.entity.User;
import com.nvminh162.userservice.mapper.UserMapper;
import com.nvminh162.userservice.repository.KeycloakClient;
import com.nvminh162.userservice.repository.UserRepository;
import com.nvminh162.userservice.service.IUserService;
import com.nvminh162.userservice.utils.KeycloakUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService implements IUserService {

    UserRepository userRepository;
    KeycloakClient keycloakClient;
    UserMapper userMapper;

    @NonFinal
    @Value("${keycloak.realm}")
    String realm;

    @NonFinal
    @Value("${keycloak.client-id}")
    String clientId;

    @NonFinal
    @Value("${keycloak.client-secret}")
    String clientSecret;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        log.info(">>> REALML: {}, CLIENT_ID: {}, CLIENT_SECRET: {}", realm, clientId, clientSecret);

        TokenExchangeResponse token = keycloakClient.exchangeToken(
                realm,
                TokenExchangeParam.builder()
                        .grant_type("client_credentials")
                        .client_id(clientId)
                        .client_secret(clientSecret)
                        .scope("openid")
                        .build());
        log.info(">>> Token exchange response: {}", token);

        ResponseEntity<?> userCreationResponse = keycloakClient.createUser(
                realm,
                "Bearer " + token.getAccessToken(),
                UserCreationParam.builder()
                        .username(request.getUsername())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .email(request.getEmail())
                        .enabled(true)
                        .emailVerified(false)
                        .credentials(List.of(Credential.builder()
                                .type("password")
                                .temporary(false)
                                .value(request.getPassword())
                                .build()))
                        .build());
        log.info(">>> User creation response: {}", userCreationResponse);

        String userId = KeycloakUtils.extractUserId(userCreationResponse);
        log.info(">>> User ID: {}", userId);

        User user = userMapper.toUser(request);
        user.setUserId(userId);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Override
    public UserResponse getUserById(String id) {
        return userRepository.findByUserId(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    @Override
    public UserResponse updateUser(String id, UserUpdatenRequest request) {
        User user = userRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userMapper.updateUser(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(String id) {
        User user = userRepository.findByUserId(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        userRepository.delete(user);
    }

    @Override
    public UserTokenExchangeResponse login(LoginRequest request) {
        return keycloakClient.exchangeUserToken(realm, UserTokenExchangeParam.builder()
                .grant_type("password")
                .client_id(clientId)
                .client_secret(clientSecret)
                .scope("openid")
                .username(request.getUsername())
                .password(request.getPassword())
                .build());      
    }
}
