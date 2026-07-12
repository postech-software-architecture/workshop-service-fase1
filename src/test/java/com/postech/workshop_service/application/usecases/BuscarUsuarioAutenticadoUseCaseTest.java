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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarUsuarioAutenticadoUseCaseTest {

	@Mock
	private ContextoSegurancaProvider contextoSegurancaProvider;

	@InjectMocks
	private BuscarUsuarioAutenticadoUseCase useCase;

	@Test
	void shouldReturnAuthenticatedUserData() {
		UUID id = UUID.randomUUID();
		UUID clienteId = UUID.randomUUID();
		when(contextoSegurancaProvider.identidadeAtual())
			.thenReturn(Optional.of(new IdentidadeAutenticada(id, "cliente1", clienteId, Set.of(Role.CLIENTE))));

		ResultadoUsuarioAutenticado resultado = useCase.executar();

		assertEquals(id, resultado.getId());
		assertEquals(clienteId, resultado.getClienteId());
	}

	@Test
	void shouldRequireClienteLinkForClienteContext() {
		when(contextoSegurancaProvider.identidadeAtual()).thenReturn(
				Optional.of(new IdentidadeAutenticada(UUID.randomUUID(), "admin", null, Set.of(Role.ADMINISTRADOR))));

		assertThrows(AcessoNegadoException.class, useCase::obterClienteIdObrigatorio);
	}

	@Test
	void shouldRejectClienteRoleWithoutClienteId() {
		when(contextoSegurancaProvider.identidadeAtual()).thenReturn(
				Optional.of(new IdentidadeAutenticada(UUID.randomUUID(), "cliente", null, Set.of(Role.CLIENTE))));

		assertThrows(AcessoNegadoException.class, useCase::obterClienteIdObrigatorio);
	}

	@Test
	void shouldReturnRequiredClienteId() {
		UUID clienteId = UUID.randomUUID();
		when(contextoSegurancaProvider.identidadeAtual()).thenReturn(
				Optional.of(new IdentidadeAutenticada(UUID.randomUUID(), "cliente1", clienteId, Set.of(Role.CLIENTE))));

		assertEquals(clienteId, useCase.obterClienteIdObrigatorio());
	}

	@Test
	void shouldRejectMissingAuthentication() {
		when(contextoSegurancaProvider.identidadeAtual()).thenReturn(Optional.empty());

		assertThrows(AcessoNegadoException.class, useCase::executar);
	}

}
