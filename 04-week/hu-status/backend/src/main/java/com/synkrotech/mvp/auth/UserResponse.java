package com.synkrotech.mvp.auth;

import java.util.UUID;

/** What the picker and the login echo return. */
public record UserResponse(UUID id, String name, Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getRole());
    }
}
