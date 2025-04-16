package com.example.orientcar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;        // unikalny e-mail

    @Column(nullable = false)
    private String passwordHash; // zahashowane hasło

    private String role = "USER";  // default

    private boolean enabled = false; // aktywowane po 2FA

    // Kod weryfikacyjny i data wygaśnięcia do 2FA
    private String twoFactorCode;
    private Long twoFactorExpiry;
}