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
public class RegisterRequest {

    @NotBlank(message = MessageConstants.USERNAME_EMPTY)
    @Size(min = 3, max = 50, message = MessageConstants.USER_TOO_SHORT)
    private String username;

    @NotBlank(message = MessageConstants.PASSWORD_EMPTY)
    @Size(min = 6, max = 100, message = MessageConstants.PASSWORD_TOO_SHORT)
    private String password;

    @NotBlank(message = MessageConstants.EMAIL_EMPTY)
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = MessageConstants.PHONE_EMPTY)
    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("is_active")
    private boolean isActive;

    @NotEmpty(message = MessageConstants.ROLE_EMPTY)
    private Set<String> role; // A set of role names (e.g., "ADMIN", "USER")
}
