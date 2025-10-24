package com.mycompany.evmc.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    public SecurityConfig(@Lazy AuthenticationProvider authenticationProvider,
                          @Lazy JwtAuthFilter jwtAuthFilter,
                          @Lazy CustomAuthEntryPoint customAuthEntryPoint,
                          @Lazy OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthFilter = jwtAuthFilter;
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/google").permitAll()
                        // OAuth2 callback
                        .requestMatchers("/login/oauth2/code/**", "/oauth2/authorization/**").permitAll()

                        // Rest of your existing configurations
                        .requestMatchers("/api/health").permitAll()
                        // Public recipe endpoints
                        .requestMatchers(HttpMethod.GET, "/api/recipes", "/api/recipes/trending",
                                "/api/recipes/recommended", "/api/recipes/seasonal",
                                "/api/recipes/advanced-search", "/api/recipes/*/brief",
                                "/api/recipes/*").permitAll()
                        // Public ingredient endpoints
                        .requestMatchers(HttpMethod.GET, "/api/ingredients", "/api/ingredients/categories",
                                "/api/ingredients/*", "/api/ingredients/*/substitutes").permitAll()
                        // Public review endpoints
                        .requestMatchers(HttpMethod.GET, "/api/reviews/recipe/*").permitAll()
                        // H2 console and other public resources
                        .requestMatchers("/h2-console/**", "/api/public/**", "/api/home/**", "/").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/swagger-resources/**", "/webjars/**", "/openapi").permitAll()
                        // Admin-only endpoints
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        // Protected recipe endpoints
                        .requestMatchers(HttpMethod.POST, "/api/recipes").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/recipes/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/recipes/*").authenticated()
                        .requestMatchers("/api/recipes/*/favorite").authenticated()
                        // Protected ingredient endpoints
                        .requestMatchers(HttpMethod.POST, "/api/ingredients", "/api/ingredients/*/substitutes/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/ingredients/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/ingredients/*", "/api/ingredients/*/substitutes/*").authenticated()
                        // Protected review endpoints
                        .requestMatchers("/api/reviews/user").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/reviews/recipe/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/reviews/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/reviews/*").authenticated()
                        // Protected shopping list endpoints - all require authentication
                        .requestMatchers("/api/shopping-lists/**").authenticated()
                        // Protected user endpoints - all require authentication
                        .requestMatchers("/api/users/**").authenticated()
                        // Any other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthEntryPoint))
                // Add OAuth2 login configuration
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}