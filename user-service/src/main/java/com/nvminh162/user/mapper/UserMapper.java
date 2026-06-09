package com.nvminh162.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.nvminh162.user.dto.request.UserCreationRequest;
import com.nvminh162.user.dto.request.UserUpdatenRequest;
import com.nvminh162.user.dto.response.UserResponse;
import com.nvminh162.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toUser(UserCreationRequest request);

    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserUpdatenRequest request);
}
