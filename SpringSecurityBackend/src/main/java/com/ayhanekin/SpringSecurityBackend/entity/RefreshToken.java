package com.ayhanekin.SpringSecurityBackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    Refresh token is not a JWT. It's a simple UUID string.
    Its only purpose is to prove that the user has logged in before.
    Backend checks if it exists in the database and if it's not expired.
    If valid, a new access token (JWT) is issued.
    It does not carry any user information inside — it doesn't need to.
    UUID is used instead of simple numbers (1, 2, 3) because
    sequential numbers are predictable and can be guessed by attackers.
    */
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiresAt;
}
