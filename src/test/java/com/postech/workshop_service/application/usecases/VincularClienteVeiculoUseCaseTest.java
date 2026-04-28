package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import com.postech.workshop_service.domain.valueobjects.Placa;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VincularClienteVeiculoUseCaseTest {

	@Mock
	private VeiculoRepository veiculoRepository;

	@Mock
	private ClienteRepository clienteRepository;

	@InjectMocks
	private VincularClienteVeiculoUseCase vincularClienteVeiculoUseCase;

	@Test
	void shouldLinkNewClienteToVeiculo() {
		UUID clienteAtual = UUID.randomUUID();
		UUID novoCliente = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();
		Veiculo veiculo = new Veiculo(veiculoId, new Placa("BRA1D23"), "Toyota", "Corolla", 2020, null, null,
				List.of(clienteAtual));

		when(veiculoRepository.buscarPorId(veiculoId, true)).thenReturn(Optional.of(veiculo));
		when(clienteRepository.buscarPorId(novoCliente)).thenReturn(Optional
			.of(new Cliente(novoCliente, "Cliente", new Documento("98765432100"), "email@teste.com", null)));
		when(veiculoRepository.salvar(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Veiculo atualizado = vincularClienteVeiculoUseCase.executar(veiculoId, novoCliente);

		assertEquals(List.of(clienteAtual, novoCliente), atualizado.getClientesVinculados().stream().toList());
	}

	@Test
	void shouldThrowWhenClienteDoesNotExist() {
		UUID veiculoId = UUID.randomUUID();
		UUID clienteAtual = UUID.randomUUID();
		UUID clienteId = UUID.randomUUID();
		Veiculo veiculo = new Veiculo(veiculoId, new Placa("BRA1D23"), "Toyota", "Corolla", 2020, null, null,
				List.of(clienteAtual));

		when(veiculoRepository.buscarPorId(veiculoId, true)).thenReturn(Optional.of(veiculo));

		assertThrows(RegraDeNegocioException.class, () -> vincularClienteVeiculoUseCase.executar(veiculoId, clienteId));
	}

	@Test
	void shouldThrowWhenVeiculoDoesNotExist() {
		assertThrows(RecursoNaoEncontradoException.class,
				() -> vincularClienteVeiculoUseCase.executar(UUID.randomUUID(), UUID.randomUUID()));
	}

}
