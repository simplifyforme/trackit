package com.example.template.user.mapper;

import com.example.template.user.dto.UserResponse;
import com.example.template.user.entity.Role;
import com.example.template.user.entity.User;
import org.mapstruct.Mapper;

/**
 * MapStruct generates the implementation at compile time.
 * componentModel="spring" is set globally via the -Amapstruct.defaultComponentModel compiler arg in pom.xml.
 *
 * The default method below tells MapStruct how to convert a single Role → String,
 * and MapStruct automatically applies it element-wise for the Set<Role> → Set<String> mapping.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toDto(User user);

    default String roleToString(Role role) {
        return role.getName();
    }
}
