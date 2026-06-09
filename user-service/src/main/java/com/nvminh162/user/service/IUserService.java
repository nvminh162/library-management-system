package com.nvminh162.user.service;

import java.util.List;

import com.nvminh162.user.dto.keycloak.LoginRequest;
import com.nvminh162.user.dto.keycloak.UserTokenExchangeResponse;
import com.nvminh162.user.dto.request.UserCreationRequest;
import com.nvminh162.user.dto.request.UserUpdatenRequest;
import com.nvminh162.user.dto.response.UserResponse;

public interface IUserService {

    UserResponse createUser(UserCreationRequest request);

    List<UserResponse> getAllUsers();

    UserResponse getUserById(String id);

    UserResponse updateUser(String id, UserUpdatenRequest dto);

    void deleteUser(String id);

    UserTokenExchangeResponse login(LoginRequest request);
}
