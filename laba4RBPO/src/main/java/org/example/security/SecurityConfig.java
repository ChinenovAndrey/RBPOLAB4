package org.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler) throws Exception {

        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()));

        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.httpBasic(basic -> basic
                .authenticationEntryPoint(authenticationEntryPoint));

        http.exceptionHandling(handling -> handling
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler));

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/error").permitAll();
            auth.requestMatchers(HttpMethod.GET, "/api/csrf").permitAll();
            auth.requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll();

            auth.requestMatchers(HttpMethod.GET, "/api/users/*", "/api/users/*/full-profile").authenticated();
            auth.requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN");

            auth.requestMatchers("/api/suppliers", "/api/suppliers/**").hasAnyRole("MANAGER", "ADMIN");
            auth.requestMatchers("/api/purchase-requests", "/api/purchase-requests/**").authenticated();
            auth.requestMatchers("/api/commercial-offers", "/api/commercial-offers/**").hasAnyRole("MANAGER", "ADMIN");
            auth.requestMatchers("/api/purchase-orders", "/api/purchase-orders/**").hasAnyRole("MANAGER", "ADMIN");

            auth.requestMatchers(HttpMethod.POST, "/api/operations/requests/*/cancel").authenticated();
            auth.requestMatchers(HttpMethod.POST, "/api/operations/suppliers/*/revoke-approval").hasRole("ADMIN");
            auth.requestMatchers("/api/operations/**").hasAnyRole("MANAGER", "ADMIN");

            auth.anyRequest().authenticated();
        });

        return http.build();
    }
}