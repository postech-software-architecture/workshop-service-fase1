package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarOrdemServicoUseCaseTest {

	@Mock
	private ClienteRepository clienteRepository;

	@Mock
	private VeiculoRepository veiculoRepository;

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@InjectMocks
	private CriarOrdemServicoUseCase useCase;

	@Test
	void shouldCreateOsInRecebidoStatusForExistingClientAndVehicle() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculo("ABC1D23", cliente.getId());

		when(clienteRepository.buscarPorDocumento(anyString(), anyBoolean())).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("12345678909")
			.veiculoPlaca("ABC1D23")
			.observacoes("Barulho ao frear")
			.build();

		ResultadoCriacaoOrdemServico resultado = useCase.executar(request);

		assertNotNull(resultado.ordemServico());
		assertEquals(StatusOrdemServico.RECEBIDO, resultado.ordemServico().getStatus());
		assertEquals("OS-2026-00001", resultado.ordemServico().getNumero());
		assertEquals("Barulho ao frear", resultado.ordemServico().getObservacoes());
		assertNull(resultado.orcamento());
	}

	@Test
	void shouldRejectWhenClientNotFound() {
		when(clienteRepository.buscarPorDocumento(anyString(), anyBoolean())).thenReturn(Optional.empty());

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("00000000000")
			.veiculoPlaca("ABC1D23")
			.build();

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(request));
	}

	@Test
	void shouldRejectWhenVehicleDoesNotBelongToClient() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculoDeOutro = criarVeiculo("ABC1D23", UUID.randomUUID());

		when(clienteRepository.buscarPorDocumento(anyString(), anyBoolean())).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculoDeOutro));

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("12345678909")
			.veiculoPlaca("ABC1D23")
			.build();

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(request));
	}

	@Test
	void shouldRegisterNewVehicleWhenNotFoundAndDataProvided() {
		Cliente cliente = criarCliente("12345678909");

		when(clienteRepository.buscarPorDocumento(anyString(), anyBoolean())).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.empty());
		when(veiculoRepository.salvar(any(Veiculo.class))).thenAnswer(inv -> inv.getArgument(0));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("12345678909")
			.veiculoPlaca("ABC1D23")
			.veiculo(CriarOrdemServicoRequest.DadosVeiculoRequest.builder()
				.marca("Toyota")
				.modelo("Corolla")
				.ano(2020)
				.build())
			.build();

		ResultadoCriacaoOrdemServico resultado = useCase.executar(request);

		assertNotNull(resultado.veiculo());
		assertEquals("ABC1D23", resultado.veiculo().getPlaca().getValor());
	}

	@Test
	void shouldRejectWhenVehicleNotFoundAndNoDataProvided() {
		Cliente cliente = criarCliente("12345678909");

		when(clienteRepository.buscarPorDocumento(anyString(), anyBoolean())).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.empty());

		CriarOrdemServicoRequest request = CriarOrdemServicoRequest.builder()
			.clienteDocumento("12345678909")
			.veiculoPlaca("ABC1D23")
			.build();

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(request));
	}

	private Cliente criarCliente(String documento) {
		return new Cliente(UUID.randomUUID(), "Cliente Teste", new Documento(documento), "cliente@example.com",
				"11999999999");
	}

	private Veiculo criarVeiculo(String placa, UUID clienteId) {
		return new Veiculo(placa, "Toyota", "Corolla", 2020, null, null, List.of(clienteId));
	}

}
