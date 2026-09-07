package com.synkrotech.mvp.auth;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record LoginRequest(@NotNull(message = "userId is required") UUID userId) {
}
