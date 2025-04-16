package com.example.orientcar.controller;

import com.example.orientcar.model.User;
import com.example.orientcar.repository.UserRepository;
import com.example.orientcar.security.JwtService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.jwt.secret}")
    private String secretKey;

    // REJESTRACJA
    @PostMapping("/register")
    public String register(
            @RequestParam String email,
            @RequestParam String password
    ) {
        if (userRepository.findByEmail(email) != null) {
            return "Użytkownik z tym adresem e-mail już istnieje.";
        }
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        String code = String.format("%06d", new Random().nextInt(999999));
        long expiry = Instant.now().plusSeconds(600).toEpochMilli(); // 10 minut

        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(hash);
        user.setEnabled(false);
        user.setTwoFactorCode(code);
        user.setTwoFactorExpiry(expiry);
        user.setRole("USER");
        userRepository.save(user);

        // Wyślij mail
        sendEmail(email, "Kod aktywacyjny OrientCar", "Twój kod: " + code);

        return "Rejestracja OK. Sprawdź e-mail. Kod weryfikacyjny przesłano na " + email;
    }

    // POTWIERDZENIE KODU 2FA
    @GetMapping("/confirm")
    public String confirm(
            @RequestParam String email,
            @RequestParam String code
    ) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "Nie znaleziono użytkownika.";
        }
        if (user.isEnabled()) {
            return "Konto jest już aktywne.";
        }
        if (!code.equals(user.getTwoFactorCode())) {
            return "Błędny kod.";
        }
        if (Instant.now().toEpochMilli() > user.getTwoFactorExpiry()) {
            return "Kod wygasł. Zarejestruj się ponownie.";
        }
        // Aktywacja
        user.setEnabled(true);
        user.setTwoFactorCode(null);
        user.setTwoFactorExpiry(null);
        userRepository.save(user);
        return "Konto zostało aktywowane. Teraz możesz się zalogować.";
    }

    // LOGOWANIE -> jeśli user aktywny, generujemy JWT
    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password
    ) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "Błędne dane logowania.";
        }
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            return "Błędne dane logowania.";
        }
        if (!user.isEnabled()) {
            return "Konto nie zostało aktywowane.";
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return "Zalogowano pomyślnie. Twój token: " + token;
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, false);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
