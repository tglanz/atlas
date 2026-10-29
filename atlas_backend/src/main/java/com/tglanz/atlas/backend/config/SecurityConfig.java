package com.tglanz.atlas.backend.config;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.CacheControlHeadersWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.UriUtils;

/**
 * Session-based authentication. Login: {@code POST /api/auth/login} (form fields {@code email},
 * {@code password}). Logout: {@code POST /api/auth/logout}. Unsafe requests must send the {@code
 * XSRF-TOKEN} cookie value in the {@code X-XSRF-TOKEN} header.
 *
 * <p>Public: the UI's static files, reading movements and their graph, registering. Authenticated:
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
        // Never cache API responses. Static UI files set their own caching.
        .headers(
            headers ->
                headers
                    .cacheControl(cache -> cache.disable())
                    .addHeaderWriter(
                        new DelegatingRequestMatcherHeaderWriter(
                            PathPatternRequestMatcher.withDefaults().matcher("/api/**"),
                            new CacheControlHeadersWriter())))
        .formLogin(
            form ->
                form.loginProcessingUrl("/api/auth/login")
                    .usernameParameter("email")
                    .successHandler(
                        (request, response, auth) -> {
                          if (wantsHtml(request))
                            response.sendRedirect(safeReturnTo(request.getParameter("returnTo")));
                          else response.setStatus(HttpStatus.NO_CONTENT.value());
                        })
                    .failureHandler(
                        (request, response, e) -> {
                          if (wantsHtml(request))
                            response.sendRedirect(
                                "/login?error&returnTo="
                                    + UriUtils.encode(
                                        safeReturnTo(request.getParameter("returnTo")),
                                        StandardCharsets.UTF_8));
                          else response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        }))
        .logout(
            logout ->
                logout
                    .logoutUrl("/api/auth/logout")
                    .logoutSuccessHandler(
                        (request, response, auth) -> {
                          if (wantsHtml(request)) response.sendRedirect("/");
                          else response.setStatus(HttpStatus.NO_CONTENT.value());
                        }))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                    (request, response, exception) -> {
                      if (request.getRequestURI().equals("/api")
                          || request.getRequestURI().startsWith("/api/"))
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                      else {
                        String target =
                            request.getRequestURI()
                                + (request.getQueryString() == null
                                    ? ""
                                    : "?" + request.getQueryString());
                        response.sendRedirect(
                            "/login?returnTo=" + UriUtils.encode(target, StandardCharsets.UTF_8));
                      }
                    }))
        .authorizeHttpRequests(
            auth ->
                auth.dispatcherTypeMatchers(DispatcherType.ERROR)
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/auth/register", "/register")
                    .permitAll()
                    .requestMatchers("/api/auth/me", "/api/routines/**")
                    .authenticated()
                    .requestMatchers(HttpMethod.GET, "/api/movements/*/generations/**")
                    .hasRole(MAINTAINER)
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/movements/**",
                        "/api/movement-index/**",
                        "/api/graph/**")
                    .permitAll()
                    .requestMatchers(
                        "/api/movements/**",
                        "/api/graph/**",
                        "/api/avatars/**",
                        "/api/viewpoints/**")
                    .hasRole(MAINTAINER)
                    // The API reference lists every endpoint, so only maintainers see it.
                    .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                    .hasRole(MAINTAINER)
                    .requestMatchers("/api/**")
                    .denyAll()
                    .requestMatchers(HttpMethod.GET, "/routines/shared/*")
                    .permitAll()
                    .requestMatchers(HttpMethod.HEAD, "/routines/shared/*")
                    .permitAll()
                    .requestMatchers("/routines", "/routines/**")
                    .authenticated()
                    .requestMatchers(
                        "/settings/**",
                        "/movements/new",
                        "/movements/*/edit",
                        "/movements/*/generations/**",
                        "/movements/*/generation-status",
                        "/movements/*/illustrations/**")
                    .hasRole(MAINTAINER)
                    .requestMatchers(HttpMethod.POST, "/movements/**")
                    .hasRole(MAINTAINER)
                    .requestMatchers(HttpMethod.GET, "/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.HEAD, "/**")
                    .permitAll()
                    .anyRequest()
                    .denyAll());
    return http.build();
  }

  private static boolean wantsHtml(HttpServletRequest request) {
    String accept = request.getHeader("Accept");
    return accept != null && accept.contains("text/html");
  }

  private static String safeReturnTo(String path) {
    if (path == null
        || !path.startsWith("/")
        || path.startsWith("//")
        || path.contains("\\")
        || path.contains("\r")
        || path.contains("\n")) return "/movements";
    return path.matches("/(?:movements|routines|settings)(?:[/?].*)?") ? path : "/movements";
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
