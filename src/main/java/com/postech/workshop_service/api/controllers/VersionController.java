package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.VersionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint publico que expoe a versao da aplicacao em execucao. A versao vem do
 * build-info gerado pelo Maven (project.version), permitindo confirmar apos um deploy
 * qual build esta efetivamente no ar. Nao exige autenticacao.
 */
@RestController
@RequestMapping("/version")
@Tag(name = "Version", description = "Versao da aplicacao em execucao")
public class VersionController {

	/**
	 * Valor retornado quando o build-info nao esta disponivel (ex.: execucao via IDE).
	 */
	private static final String VERSAO_DESCONHECIDA = "unknown";

	private final ObjectProvider<BuildProperties> buildProperties;

	/**
	 * Construtor para injecao de dependencias.
	 * @param buildProperties provedor opcional do build-info (ausente sem o goal
	 * build-info do Maven).
	 */
	public VersionController(ObjectProvider<BuildProperties> buildProperties) {
		this.buildProperties = buildProperties;
	}

	/**
	 * Retorna a versao da aplicacao em execucao.
	 * @return versao lida do build-info, ou {@code unknown} se indisponivel.
	 */
	@GetMapping
	@Operation(summary = "Retorna a versao da aplicacao em execucao")
	public VersionResponse versao() {
		BuildProperties build = this.buildProperties.getIfAvailable();
		String versao = (build != null) ? build.getVersion() : VERSAO_DESCONHECIDA;
		return new VersionResponse(versao);
	}

}
