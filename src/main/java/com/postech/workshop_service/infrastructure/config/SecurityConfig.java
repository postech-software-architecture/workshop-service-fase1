package com.postech.workshop_service.infrastructure.config;

import com.postech.workshop_service.infrastructure.security.DetalhesUsuarioServiceImpl;
import com.postech.workshop_service.infrastructure.security.JwtAccessDeniedHandler;
import com.postech.workshop_service.infrastructure.security.JwtAuthenticationEntryPoint;
import com.postech.workshop_service.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuracao de seguranca do servico.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	private final DetalhesUsuarioServiceImpl detalhesUsuarioService;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
			JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler,
			DetalhesUsuarioServiceImpl detalhesUsuarioService) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
		this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
		this.detalhesUsuarioService = detalhesUsuarioService;
	}

	/**
	 * Configura a cadeia de filtros de seguranca com autenticacao JWT e autorizacao por
	 * perfis.
	 * @param http configurador de seguranca HTTP.
	 * @return cadeia de filtros configurada.
	 * @throws Exception caso ocorra erro na configuracao.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authenticationProvider(authenticationProvider())
			.exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				.accessDeniedHandler(jwtAccessDeniedHandler))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/api/auth/login", "/api/auth/refresh",
						"/api/auth/logout", "/api/public/**", "/api/v1/ordens-servico/*/status", "/api/v1/webhooks/**",
						"/actuator/health/**", "/actuator/info")
				.permitAll()
				.anyRequest()
				.authenticated())
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	/**
	 * Provider baseado em usuario/senha para o fluxo de login.
	 * @return provider configurado.
	 */
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(detalhesUsuarioService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	/**
	 * Encoder BCrypt de senhas.
	 * @return encoder configurado.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Authentication manager da aplicacao.
	 * @param configuration configuracao de autenticacao do Spring.
	 * @return manager configurado.
	 * @throws Exception caso ocorra erro na construcao.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

}
