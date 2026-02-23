package com.nvminh162.userservice.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.nvminh162.userservice.dto.request.UserCreationRequest;
import com.nvminh162.userservice.dto.request.UserUpdatenRequest;
import com.nvminh162.userservice.dto.response.UserResponse;
import com.nvminh162.userservice.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserUpdatenRequest request);
}
