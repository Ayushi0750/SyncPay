

package com.project.offline_payment_sync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String email;
    private String token;
    private String role;
    private Long expiresIn; 

    
    public LoginResponse(String token) {
        this.token = token;
    }
}