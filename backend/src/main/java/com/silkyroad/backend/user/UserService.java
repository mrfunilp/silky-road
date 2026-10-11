package com.silkyroad.backend.user;

import com.silkyroad.backend.common.DuplicateResourceException;
import com.silkyroad.backend.common.NotFoundException;
import com.silkyroad.backend.common.UnauthorizedException;
import com.silkyroad.backend.user.dto.AuthResponse;
import com.silkyroad.backend.user.dto.CreateUserRequest;
import com.silkyroad.backend.user.dto.LoginRequest;
import com.silkyroad.backend.user.dto.UserResponse;
import java.util.List;
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

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public List<UserResponse> findAll() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    public UserResponse findById(UUID id) {
        return userMapper.toResponse(getOrThrow(id));
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

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.username().trim())
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));

        TokenService.IssuedToken issued = tokenService.issue(user);
        return new AuthResponse(issued.token(), issued.expiresIn(), userMapper.toResponse(user));
    }
}
