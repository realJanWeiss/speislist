package com.speislist.backend.user.util;

import com.speislist.backend.user.entity.User;
import com.speislist.backend.user.dto.UserDTO;

public class UserMapper {

    private UserMapper() {}

    public static UserDTO toUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        return userDTO;
    }
}
