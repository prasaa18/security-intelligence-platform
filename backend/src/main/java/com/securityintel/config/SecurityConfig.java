package com.securityintel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("secure")
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/integrations/scans/github-actions/**").permitAll()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    InMemoryUserDetailsManager userDetailsService(
            @Value("${security.users.admin.username}") String adminUsername,
            @Value("${security.users.admin.password}") String adminPassword,
            @Value("${security.users.viewer.username}") String viewerUsername,
            @Value("${security.users.viewer.password}") String viewerPassword) {
        UserDetails admin = User.withUsername(adminUsername)
            .password(adminPassword)
            .roles("ADMIN", "SECURITY_LEAD")
            .build();
        UserDetails viewer = User.withUsername(viewerUsername)
            .password(viewerPassword)
            .roles("VIEWER")
            .build();
        return new InMemoryUserDetailsManager(admin, viewer);
    }
}