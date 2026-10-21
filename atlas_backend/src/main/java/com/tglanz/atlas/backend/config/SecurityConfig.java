package com.tglanz.atlas.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Session-based authentication. Login: {@code POST /api/auth/login} (form fields {@code email},
 * {@code password}). Logout: {@code POST /api/auth/logout}. Unsafe requests must send the {@code
 * XSRF-TOKEN} cookie value in the {@code X-XSRF-TOKEN} header.
 *
 * <p>Public: reading published movements and avatars (names only), registering. Authenticated:
 * routines and the current user. Maintainer: everything else.
 */
@Configuration
class SecurityConfig {

  static final String MAINTAINER = "MAINTAINER";

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(
            csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
        .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
        .formLogin(
            form ->
                form.loginProcessingUrl("/api/auth/login")
                    .usernameParameter("email")
                    .successHandler(
                        (request, response, auth) ->
                            response.setStatus(HttpStatus.NO_CONTENT.value()))
                    .failureHandler(
                        (request, response, e) ->
                            response.setStatus(HttpStatus.UNAUTHORIZED.value())))
        .logout(
            logout ->
                logout
                    .logoutUrl("/api/auth/logout")
                    .logoutSuccessHandler(
                        new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))
        .exceptionHandling(
            e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/api/auth/register")
                    .permitAll()
                    .requestMatchers("/api/auth/me/**", "/api/auth/me", "/api/routines/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/movements/*/draft/**")
                    .hasRole(MAINTAINER)
                    .requestMatchers(HttpMethod.GET, "/api/movements/**", "/api/avatars/**")
                    .permitAll()
                    .requestMatchers("/api/movements/**", "/api/avatars/**", "/api/viewpoints/**")
                    .hasRole(MAINTAINER)
                    .anyRequest()
                    .denyAll());
    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  /** Makes sure the CSRF cookie is sent, since the token is otherwise loaded lazily. */
  private static final class CsrfCookieFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
      CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
      if (token != null) {
        token.getToken();
      }
      chain.doFilter(request, response);
    }
  }
}
