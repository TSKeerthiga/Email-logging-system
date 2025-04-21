package com.ideas2it.emailLoggingSystem.service;

import com.ideas2it.emailLoggingSystem.dto.LoginRequest;
import com.ideas2it.emailLoggingSystem.dto.RegisterRequest;

import com.ideas2it.emailLoggingSystem.dto.ResponseResult;
import com.ideas2it.emailLoggingSystem.entity.Users;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

public interface AuthService {

    ResponseResult register(RegisterRequest registerRequest, BindingResult bindingResult);

    ResponseResult login(LoginRequest loginRequest, BindingResult bindingResult);

    ResponseEntity<String> logout(HttpServletRequest request);

}
