package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.VersionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VersionControllerTest {

	@Test
	void deveRetornarVersaoDoBuildQuandoDisponivel() {
		Properties propriedades = new Properties();
		propriedades.setProperty("version", "1.2.3");
		ObjectProvider<BuildProperties> provider = providerDe(new BuildProperties(propriedades));

		VersionResponse resposta = new VersionController(provider).versao();

		assertThat(resposta.version()).isEqualTo("1.2.3");
	}

	@Test
	void deveRetornarUnknownQuandoBuildInfoIndisponivel() {
		ObjectProvider<BuildProperties> provider = providerDe(null);

		VersionResponse resposta = new VersionController(provider).versao();

		assertThat(resposta.version()).isEqualTo("unknown");
	}

	@SuppressWarnings("unchecked")
	private ObjectProvider<BuildProperties> providerDe(BuildProperties valor) {
		ObjectProvider<BuildProperties> provider = mock(ObjectProvider.class);
		when(provider.getIfAvailable()).thenReturn(valor);
		return provider;
	}

}
