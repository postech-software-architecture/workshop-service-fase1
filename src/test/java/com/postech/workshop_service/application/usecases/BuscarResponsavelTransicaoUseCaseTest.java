package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import com.postech.workshop_service.domain.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarResponsavelTransicaoUseCaseTest {

	@Mock
	private ContextoSegurancaProvider contextoSegurancaProvider;

	@InjectMocks
	private BuscarResponsavelTransicaoUseCase useCase;

	@Test
	void shouldUseAuthenticatedUserAsResponsible() {
		UUID id = UUID.randomUUID();
		when(contextoSegurancaProvider.identidadeAtual())
			.thenReturn(Optional.of(new IdentidadeAutenticada(id, "mecanico", null, Set.of(Role.MECANICO))));

		ResponsavelTransicao responsavel = useCase.executar();

		assertEquals(id, responsavel.idUsuario());
		assertEquals("mecanico", responsavel.username());
	}

	@Test
	void shouldFailClosedWhenNoUserAndNoSystemActor() {
		when(contextoSegurancaProvider.identidadeAtual()).thenReturn(Optional.empty());

		assertThrows(AcessoNegadoException.class, () -> useCase.executar());
	}

	@Test
	void shouldUseSystemActorWhenMarkedAndNoUser() {
		when(contextoSegurancaProvider.identidadeAtual()).thenReturn(Optional.empty());

		ResponsavelTransicao responsavel = AtorSistemaContext.executarComo("webhook:teste", () -> useCase.executar());

		assertNotNull(responsavel.idUsuario());
		assertEquals("webhook:teste", responsavel.username());
	}

}
