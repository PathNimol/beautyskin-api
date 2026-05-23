package com.acleda.bsonlineshop.dto.auth;

import com.acleda.bsonlineshop.validation.PasswordPolicy;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    private String phone;

    @NotBlank
    @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH, message = PasswordPolicy.MESSAGE)
    private String password;

    @NotBlank
    private String confirmPassword;

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}
