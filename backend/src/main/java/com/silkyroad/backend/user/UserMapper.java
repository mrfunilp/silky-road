package com.silkyroad.backend.user;

import com.silkyroad.backend.user.dto.UserProfileResponse;
import com.silkyroad.backend.user.dto.UserResponse;
import com.silkyroad.backend.user.dto.UserSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getCreatedAt());
    }

    public UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getCreatedAt());
    }

    public UserSummaryResponse toSummary(User user) {
        return new UserSummaryResponse(user.getId(), user.getUsername());
    }
}
