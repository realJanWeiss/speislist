package com.speislist.backend.user.util;

import com.speislist.backend.user.dto.UserDTO;
import com.speislist.backend.user.entity.User;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class UserMapper {

    public static UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .build();
    }
}
