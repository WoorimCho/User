package com.example.user.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The {@code security} starter is on the classpath, so an explicit chain is
 * required or every endpoint gets HTTP Basic with a generated password.
 *
 * <p>For now every request is permitted: this service trusts its network, and
 * the real question of "who may read/modify account N" is the still-open
 * inter-service auth decision (likely: the BFF authenticates the end user and
 * forwards a verified identity, and this service scopes to it). What this class
 * <em>does</em> do is provide a real {@link PasswordEncoder} so stored passwords
 * are BCrypt hashes, never plaintext.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
