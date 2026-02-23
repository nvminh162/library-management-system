package com.nvminh162.userservice.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nvminh162.userservice.dto.request.UserCreationRequest;
import com.nvminh162.userservice.dto.request.UserUpdatenRequest;
import com.nvminh162.userservice.dto.response.UserResponse;
import com.nvminh162.userservice.entity.User;
import com.nvminh162.userservice.mapper.UserMapper;
import com.nvminh162.userservice.repository.UserRepository;
import com.nvminh162.userservice.service.IUserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserService implements IUserService {

    UserRepository userRepository;
    UserMapper userMapper;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setUserId(UUID.randomUUID().toString());
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
}
