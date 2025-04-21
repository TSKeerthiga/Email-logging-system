package com.ideas2it.emailLoggingSystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ideas2it.emailLoggingSystem.constants.MessageConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = MessageConstants.USERNAME_EMPTY)
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = MessageConstants.PASSWORD_EMPTY)
    @Size(min = 6, max = 100, message = MessageConstants.PASSWORD_TOO_SHORT)
    private String password;

}
