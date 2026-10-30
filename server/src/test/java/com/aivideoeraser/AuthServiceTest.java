package com.aivideoeraser;

import com.aivideoeraser.dto.AuthResponse;
import com.aivideoeraser.dto.LoginRequest;
import com.aivideoeraser.dto.RegisterRequest;
import com.aivideoeraser.entity.Role;
import com.aivideoeraser.entity.User;
import com.aivideoeraser.entity.UserStatus;
import com.aivideoeraser.exception.BadRequestException;
import com.aivideoeraser.repository.SubscriptionRepository;
import com.aivideoeraser.repository.UserRepository;
import com.aivideoeraser.security.JwtTokenProvider;
import com.aivideoeraser.security.UserPrincipal;
import com.aivideoeraser.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encoded_pass")
                .fullName("Test User")
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .credits(50)
                .build();
    }

    @Test
    void testRegister_Success() {
        RegisterRequest req = RegisterRequest.builder()
                .email("test@example.com")
                .password("raw_pass")
                .fullName("Test User")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("raw_pass")).thenReturn("encoded_pass");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateTokenFromUser(anyLong(), anyString(), anyString())).thenReturn("mock_jwt");
        when(tokenProvider.generateRefreshToken(anyLong())).thenReturn("mock_refresh");

        AuthResponse resp = authService.register(req);

        assertNotNull(resp);
        assertEquals("test@example.com", resp.getEmail());
        assertEquals("mock_jwt", resp.getToken());
        assertEquals(50, resp.getCredits());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        RegisterRequest req = RegisterRequest.builder()
                .email("existing@example.com")
                .password("raw_pass")
                .fullName("Existing User")
                .build();

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        LoginRequest req = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        UserPrincipal principal = UserPrincipal.create(sampleUser);
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(tokenProvider.generateToken(auth)).thenReturn("jwt_token_abc");
        when(tokenProvider.generateRefreshToken(1L)).thenReturn("refresh_token_xyz");

        AuthResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("jwt_token_abc", resp.getToken());
        assertEquals("test@example.com", resp.getEmail());
    }
}
