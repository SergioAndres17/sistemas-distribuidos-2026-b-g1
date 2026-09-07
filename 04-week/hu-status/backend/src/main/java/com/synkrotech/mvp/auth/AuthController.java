package com.synkrotech.mvp.auth;

import java.util.List;

import com.synkrotech.mvp.common.BusinessException;
import com.synkrotech.mvp.common.NotFoundException;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;

    public AuthController(UserRepository users) {
        this.users = users;
    }

    /** Feeds the login picker. Only active users can be picked. */
    @GetMapping("/users")
    public List<UserResponse> list() {
        return users.findByActiveTrueOrderByNameAsc().stream()
                .map(UserResponse::from)
                .toList();
    }

    /**
     * Simulated login: no password is checked, the chosen user is simply echoed
     * back for the frontend to keep in memory as the current session.
     */
    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        User user = users.findById(request.userId())
                .orElseThrow(() -> new NotFoundException("No user exists with id " + request.userId() + "."));
        if (!user.isActive()) {
            throw new BusinessException("The user " + user.getName() + " is deactivated and cannot log in.");
        }
        return UserResponse.from(user);
    }
}
