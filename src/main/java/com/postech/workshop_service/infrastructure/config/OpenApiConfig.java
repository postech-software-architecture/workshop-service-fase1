package com.postech.workshop_service.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuracao do OpenAPI/Swagger com documentacao do uso de JWT.
 */
@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
		in = SecuritySchemeIn.HEADER, paramName = "Authorization",
		description = "Informe o access token no header Authorization no formato: Bearer {token}. "
				+ "Obtenha o token em /api/auth/login e renove a sessao em /api/auth/refresh.")
public class OpenApiConfig {

	@Bean
	public OpenAPI workshopOpenAPI() {
		return new OpenAPI().info(new Info().title("Workshop Service API")
			.description("API da oficina com autenticacao JWT via header Authorization.")
			.version("v1")
			.license(new License().name("Uso interno")));
	}

}
