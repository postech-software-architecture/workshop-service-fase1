package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarClienteUseCaseTest {

	@Mock
	private ClienteRepository clienteRepository;

	@InjectMocks
	private AtualizarClienteUseCase atualizarClienteUseCase;

	@Test
	void shouldUpdateCliente() {
		UUID id = UUID.randomUUID();
		Cliente cliente = new Cliente(id, "Antigo", new Documento("98765432100"), "email@email.com", null);

		when(clienteRepository.buscarPorId(id, true)).thenReturn(Optional.of(cliente));
		when(clienteRepository.salvar(any())).thenAnswer(i -> i.getArguments()[0]);

		Cliente updated = atualizarClienteUseCase.executar(id, "Novo", "novo@email.com", null, null, null, null);

		assertEquals("Novo", updated.getNome());
		assertEquals("novo@email.com", updated.getEmail());
	}

	@Test
	void shouldThrowExceptionWhenClienteNotFound() {
		UUID id = UUID.randomUUID();
		when(clienteRepository.buscarPorId(id, true)).thenReturn(Optional.empty());

		assertThrows(IllegalArgumentException.class,
				() -> atualizarClienteUseCase.executar(id, "Novo", "email", null, null, null, null));
	}

}
