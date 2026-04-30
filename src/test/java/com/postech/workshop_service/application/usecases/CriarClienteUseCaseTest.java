package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarClienteUseCaseTest {

	@Mock
	private ClienteRepository clienteRepository;

	@InjectMocks
	private CriarClienteUseCase criarClienteUseCase;

	@Test
	void shouldCreateCliente() {
		when(clienteRepository.existePorDocumento(any())).thenReturn(false);
		when(clienteRepository.salvar(any())).thenAnswer(i -> i.getArguments()[0]);

		Cliente cliente = criarClienteUseCase.executar("Nome", "98765432100", "email@email.com", null);

		assertNotNull(cliente);
		verify(clienteRepository).salvar(any());
	}

	@Test
	void shouldThrowExceptionWhenDocumentAlreadyExists() {
		when(clienteRepository.existePorDocumento(any())).thenReturn(true);

		assertThrows(IllegalArgumentException.class,
				() -> criarClienteUseCase.executar("Nome", "98765432100", "email@email.com", null));
	}

}
