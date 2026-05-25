package com.example.xinlingqian.dto.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    
    private Long userId;
    
    private String username;
    
    private String nickname;
    
    private String role;
}
