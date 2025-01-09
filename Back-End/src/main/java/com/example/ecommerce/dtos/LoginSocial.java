package com.example.ecommerce.dtos;

import com.example.ecommerce.enums.LoginType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginSocial {
    @NotBlank(message = "providerId cannot be empty")
    private String providerId;

    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Image cannot be empty")
    private String image;

    private LoginType loginType;

}
