package com.silkyroad.backend.user;

import com.silkyroad.backend.user.dto.AuthResponse;
import com.silkyroad.backend.user.dto.CreateUserRequest;
import com.silkyroad.backend.user.dto.LoginRequest;
import com.silkyroad.backend.user.dto.UpdateAccountRequest;
import com.silkyroad.backend.user.dto.UserProfileResponse;
import com.silkyroad.backend.user.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateAccountRequest request) {
        return userService.updateAccount(UUID.fromString(jwt.getSubject()), request);
    }

    @GetMapping("/{id}")
    public UserProfileResponse findById(@PathVariable UUID id) {
        return userService.findProfile(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.register(request);
        return ResponseEntity.created(URI.create("/api/users/" + created.id())).body(created);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return userService.login(request, httpRequest.getRemoteAddr());
    }
}
