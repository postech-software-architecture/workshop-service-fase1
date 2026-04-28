package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
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
class CriarVeiculoUseCaseTest {

	@Mock
	private VeiculoRepository veiculoRepository;

	@Mock
	private ClienteRepository clienteRepository;

	@InjectMocks
	private CriarVeiculoUseCase criarVeiculoUseCase;

	@Test
	void shouldCreateVeiculo() {
		UUID clienteId = UUID.randomUUID();
		when(clienteRepository.buscarPorId(clienteId)).thenReturn(
				Optional.of(new Cliente(clienteId, "Cliente", new Documento("98765432100"), "email@teste.com", null)));
		when(veiculoRepository.existePlacaAtiva("BRA1D23", null)).thenReturn(false);
		when(veiculoRepository.salvar(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Veiculo veiculo = criarVeiculoUseCase.executar("BRA1D23", "Toyota", "Corolla", 2020, "Prata", "Obs",
				List.of(clienteId));

		assertEquals("BRA1D23", veiculo.getPlaca().getValor());
	}

	@Test
	void shouldRejectDuplicatedActivePlate() {
		UUID clienteId = UUID.randomUUID();
		when(clienteRepository.buscarPorId(clienteId)).thenReturn(
				Optional.of(new Cliente(clienteId, "Cliente", new Documento("98765432100"), "email@teste.com", null)));
		when(veiculoRepository.existePlacaAtiva("BRA1D23", null)).thenReturn(true);

		assertThrows(RegraDeNegocioException.class, () -> criarVeiculoUseCase.executar("BRA1D23", "Toyota", "Corolla",
				2020, null, null, List.of(clienteId)));
	}

}
