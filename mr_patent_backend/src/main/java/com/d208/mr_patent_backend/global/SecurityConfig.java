package com.d208.mr_patent_backend.global;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .formLogin((form) -> form.disable())
                .httpBasic((basic) -> basic.disable())
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/user").permitAll()
                        .requestMatchers("/api/user/expert").permitAll()
                        .requestMatchers("/api/user/check-email").permitAll()
                        .requestMatchers("/api/email/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/user/expert/*").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .cors(cors -> cors.disable());

        return http.build();
    }
}
