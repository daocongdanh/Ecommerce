package com.example.ecommerce.services.auth;

import com.example.ecommerce.enums.LoginType;
import com.example.ecommerce.models.Role;
import com.example.ecommerce.models.Token;
import com.example.ecommerce.models.User;
import com.example.ecommerce.models.UserRole;
import com.example.ecommerce.repositories.RoleRepository;
import com.example.ecommerce.repositories.UserRepository;
import com.example.ecommerce.repositories.UserRoleRepository;
import com.example.ecommerce.responses.LoginResponse;
import com.example.ecommerce.responses.UserResponse;
import com.example.ecommerce.services.jwt.JwtService;
import com.example.ecommerce.services.token.TokenService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.google.user-info-uri}")
    private String googleUserInfoUri;

    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String facebookRedirectUri;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String facebookClientId;

    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String facebookClientSecret;

    @Value("${spring.security.oauth2.client.registration.facebook.auth-uri}")
    private String facebookAuthUri;

    @Value("${spring.security.oauth2.client.registration.facebook.token-uri}")
    private String facebookTokenUri;

    @Value("${spring.security.oauth2.client.registration.facebook.user-info-uri}")
    private String facebookUserInfoUri;

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final TokenService tokenService;
    @Override
    public String generateAuthUrl(String loginType) {
        String url = "";
        loginType = loginType.trim().toUpperCase();

        if(LoginType.GOOGLE.equals(LoginType.valueOf(loginType))){
            // Sinh ra đường dẫn url để gửi cho client đăng nhập
            GoogleAuthorizationCodeRequestUrl urlBuilder = new GoogleAuthorizationCodeRequestUrl(
                    googleClientId,
                    googleRedirectUri,
                    Arrays.asList("email", "profile", "openid"));
            url = urlBuilder.build();
        }
        else if(LoginType.FACEBOOK.equals(LoginType.valueOf(loginType))){
            url = UriComponentsBuilder
                    .fromUriString(facebookAuthUri)
                    .queryParam("client_id", facebookClientId)
                    .queryParam("redirect_uri", facebookRedirectUri)
                    .queryParam("scope", "email,public_profile")
                    .queryParam("response_type", "code")
                    .build()
                    .toUriString();
        }
        return url;
    }

    @Override
    public Map<String, Object> authenticateAndFetchProfile(String code, LoginType loginType) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        String accessToken;

        if(LoginType.GOOGLE.equals(loginType)){
            // Gọi lên google kèm code để lấy accessToken
            accessToken = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(), new GsonFactory(),
                    googleClientId,
                    googleClientSecret,
                    code,
                    googleRedirectUri
            ).execute().getAccessToken();

            // Đính kèm token
            restTemplate.getInterceptors().add((req, body, executionContext) -> {
                req.getHeaders().set("Authorization", "Bearer " + accessToken);
                return executionContext.execute(req, body);
            });

            // Gọi lên google để lấy info user
            return new ObjectMapper().readValue(
                    restTemplate.getForEntity(googleUserInfoUri, String.class).getBody(),
                    new TypeReference<>() {});

        }
        else if(LoginType.FACEBOOK.equals(loginType)){
            String urlGetAccessToken = UriComponentsBuilder
                    .fromUriString(facebookTokenUri)
                    .queryParam("client_id", facebookClientId)
                    .queryParam("redirect_uri", facebookRedirectUri)
                    .queryParam("client_secret", facebookClientSecret)
                    .queryParam("code", code)
                    .toUriString();

            ResponseEntity<String> response = restTemplate.getForEntity(urlGetAccessToken, String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response.getBody());
            accessToken = node.get("access_token").asText();

            String userInfoUri = facebookUserInfoUri + "&access_token=" + accessToken;
            return mapper.readValue(
                    restTemplate.getForEntity(userInfoUri, String.class).getBody(),
                    new TypeReference<>() {});
        }
        return null;
    }

    @Override
    @Transactional
    public LoginResponse loginSocial(Map<String, Object> userInfo, HttpServletRequest request, LoginType loginType) {
        String provideId = "";
        String name = "";
        String picture = "";
        String email = "";
        if(LoginType.GOOGLE.equals(loginType)){
            provideId = (String) Objects.requireNonNullElse(userInfo.get("sub"), "");
            name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
            picture = (String) Objects.requireNonNullElse(userInfo.get("picture"), "");
            email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");
        }
        else if(LoginType.FACEBOOK.equals(loginType)){
            provideId = (String) Objects.requireNonNullElse(userInfo.get("id"), "");
            name = (String) Objects.requireNonNullElse(userInfo.get("name"), "");
            email = (String) Objects.requireNonNullElse(userInfo.get("email"), "");
            // Lấy URL ảnh từ cấu trúc dữ liệu của Facebook
            Object pictureObj = userInfo.get("picture");
            if (pictureObj instanceof Map<?, ?> pictureData) {
                Object dataObj = pictureData.get("data");
                if (dataObj instanceof Map<?, ?> dataMap) {
                    Object urlObj = dataMap.get("url");
                    if (urlObj instanceof String) {
                        picture = (String) urlObj;
                    }
                }
            }
        }

        Optional<User> optionalUser = Optional.empty();
        Role role = roleRepository.findByName("user");

        if(LoginType.GOOGLE.equals(loginType)){
            optionalUser = userRepository.findByGoogleAccountId(provideId);
            // Tạo người dùng mới nếu không tìm thấy
            if (optionalUser.isEmpty()) {
                User user = User.builder()
                        .fullName(name)
                        .email(email)
                        .avatar(picture)
                        .googleAccountId(provideId)
                        .active(true)
                        .build();
                UserRole userRole = UserRole.builder()
                        .user(user)
                        .role(role)
                        .build();
                user = userRepository.save(user);
                userRoleRepository.save(userRole);
                user.setUserRoles(List.of(userRole));
                optionalUser = Optional.of(user);
            }
        }
        else if(LoginType.FACEBOOK.equals(loginType)){
            optionalUser = userRepository.findByGoogleAccountId(provideId);
            if (optionalUser.isEmpty()) {
                User user = User.builder()
                        .fullName(name)
                        .email(email)
                        .avatar(picture)
                        .facebookAccountId(provideId)
                        .active(true)
                        .build();
                UserRole userRole = UserRole.builder()
                        .user(user)
                        .role(role)
                        .build();
                user = userRepository.save(user);
                userRoleRepository.save(userRole);
                user.setUserRoles(List.of(userRole));
                optionalUser = Optional.of(user);
            }
        }
        else throw new RuntimeException("Invalid social account information.");
        User user = optionalUser.get();
        if(!user.isEnabled())
            throw new DisabledException("User account is disabled");

        String token = jwtService.generateToken(user);
        boolean isMobile = request.getHeader("User-Agent").equals("mobile");
        Token newToken = tokenService.addToken(user, token, isMobile);
        return LoginResponse.builder()
                .token(newToken.getToken())
                .refreshToken(newToken.getRefreshToken())
                .user(UserResponse.fromUser(user))
                .build();
    }
}
