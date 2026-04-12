package com.ayhanekin.SpringSecurityBackend.dto.request;

import lombok.Data;

@Data
public class RefreshRequest {
    private String refreshToken;
}