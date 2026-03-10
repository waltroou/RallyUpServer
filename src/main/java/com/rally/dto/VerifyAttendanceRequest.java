package com.rally.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyAttendanceRequest {
    
    @NotBlank(message = "Verification code is required")
    private String verificationCode;
}

