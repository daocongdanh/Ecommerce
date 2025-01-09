package com.example.ecommerce.services.auth;

import com.example.ecommerce.enums.LoginType;
import com.example.ecommerce.responses.LoginResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.Map;

public interface AuthService {
    String generateAuthUrl(String loginType);
    Map<String, Object> authenticateAndFetchProfile(String code, LoginType loginType) throws IOException;

    LoginResponse loginSocial(Map<String, Object> userInfo, HttpServletRequest request, LoginType loginType);
}
