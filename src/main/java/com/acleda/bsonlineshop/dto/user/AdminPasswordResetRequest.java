package com.acleda.bsonlineshop.dto.user;

import com.acleda.bsonlineshop.validation.PasswordPolicy;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminPasswordResetRequest {

    @NotBlank
    @Size(min = PasswordPolicy.MIN_LENGTH, max = PasswordPolicy.MAX_LENGTH, message = PasswordPolicy.MESSAGE)
    private String newPassword;

    @NotBlank
    private String confirmPassword;

    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
