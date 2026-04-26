package com.ayhanekin.SpringSecurityBackend.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

/*
Instead of returning a raw error page or an unformatted error,
this class wraps the error details into a structured JSON response
so the frontend can handle errors properly and show meaningful messages
*/
@Data
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
}
