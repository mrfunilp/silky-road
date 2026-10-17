package com.silkyroad.backend.user;

import com.silkyroad.backend.common.DuplicateResourceException;
import com.silkyroad.backend.common.NotFoundException;
import com.silkyroad.backend.common.UnauthorizedException;
import com.silkyroad.backend.user.dto.AuthResponse;
import com.silkyroad.backend.user.dto.CreateUserRequest;
import com.silkyroad.backend.user.dto.LoginRequest;
import com.silkyroad.backend.user.dto.UpdateAccountRequest;
import com.silkyroad.backend.user.dto.UserProfileResponse;
import com.silkyroad.backend.user.dto.UserResponse;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final LoginRateLimiter loginRateLimiter;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            LoginRateLimiter loginRateLimiter) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.loginRateLimiter = loginRateLimiter;
    }

    public UserProfileResponse findProfile(UUID id) {
        return userMapper.toProfile(getOrThrow(id));
    }

    public User getOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No user with id " + id));
    }

    @Transactional
    public UserResponse register(CreateUserRequest request) {
        String username = request.username().trim();
        String email = request.email().trim();

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("That username is already taken.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with that email already exists.");
        }

        User saved = userRepository.save(new User(username, email, passwordEncoder.encode(request.password())));
        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse updateAccount(UUID id, UpdateAccountRequest request) {
        User user = getOrThrow(id);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Your current password is incorrect.");
        }

        String username = request.username().trim();
        String email = request.email().trim();

        if (!username.equalsIgnoreCase(user.getUsername()) && userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("That username is already taken.");
        }
        if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("An account with that email already exists.");
        }

        user.setUsername(username);
        user.setEmail(email);
        if (request.newPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        }
        return userMapper.toResponse(user);
    }

    public AuthResponse login(LoginRequest request, String clientIp) {
        String username = request.username().trim();
        loginRateLimiter.checkAllowed(username, clientIp);

        User user = userRepository.findByUsernameIgnoreCase(username)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> {
                    loginRateLimiter.recordFailure(username, clientIp);
                    return new UnauthorizedException("Invalid username or password.");
                });

        loginRateLimiter.recordSuccess(username);

        TokenService.IssuedToken issued = tokenService.issue(user);
        return new AuthResponse(issued.token(), issued.expiresIn(), userMapper.toResponse(user));
    }
}
