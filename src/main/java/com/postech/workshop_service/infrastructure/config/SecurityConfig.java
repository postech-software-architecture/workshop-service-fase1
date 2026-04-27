package com.postech.workshop_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracao de seguranca do servico.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configura a cadeia de filtros de seguranca mantendo os endpoints liberados no MVP.
     *
     * @param http configurador de seguranca HTTP.
     * @return cadeia de filtros configurada.
     * @throws Exception caso ocorra erro na configuracao.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/v1/veiculos/**", "/api/v1/clientes/**")
                        .permitAll()
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
