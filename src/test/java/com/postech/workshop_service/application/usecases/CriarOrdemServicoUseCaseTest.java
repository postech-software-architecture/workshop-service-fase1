package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.entities.StatusOrcamento;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import com.postech.workshop_service.domain.valueobjects.Placa;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarOrdemServicoUseCaseTest {

	@Mock
	private ClienteRepository clienteRepository;

	@Mock
	private VeiculoRepository veiculoRepository;

	@Mock
	private ServicoRepository servicoRepository;

	@Mock
	private PecaInsumoRepository pecaInsumoRepository;

	@Mock
	private EstoqueRepository estoqueRepository;

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private OrcamentoRepository orcamentoRepository;

	@Mock
	private ClienteNotificationService clienteNotificationService;

	@InjectMocks
	private CriarOrdemServicoUseCase useCase;

	@Test
	void shouldCreateOsWithServicesForExistingClientAndVehicle() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());
		Servico servico = criarServico(new BigDecimal("100.00"));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoCriacaoOrdemServico resultado = useCase.executar("123.456.789-09", "ABC1D23", null, null, null,
				List.of(new ItemServicoSolicitado(servico.getId(), 1)), List.of(), null);

		assertNotNull(resultado.ordemServico());
		assertEquals("OS-2026-00001", resultado.ordemServico().getNumero());
		assertEquals(StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, resultado.ordemServico().getStatus());
		assertEquals(StatusOrcamento.PENDENTE_APROVACAO, resultado.orcamento().getStatus());
		assertEquals(new BigDecimal("100.00"), resultado.orcamento().getValor());
	}

	@Test
	void shouldCalculateBudgetSummingServicesAndParts() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());
		Servico servico = criarServico(new BigDecimal("80.00"));
		PecaInsumo peca = criarPeca(new BigDecimal("50.00"));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(pecaInsumoRepository.buscarPorId(peca.getId(), false)).thenReturn(Optional.of(peca));
		when(estoqueRepository.calcularQuantidadeTotal(peca.getId())).thenReturn(new BigDecimal("10"));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoCriacaoOrdemServico resultado = useCase.executar("12345678909", "ABC1D23", null, null, null,
				List.of(new ItemServicoSolicitado(servico.getId(), 1)),
				List.of(new ItemPecaSolicitada(peca.getId(), new BigDecimal("2"))), null);

		// R$80 servico + R$50*2 pecas = R$180
		assertEquals(new BigDecimal("180.00"), resultado.orcamento().getValor());
	}

	@Test
	void shouldCreateVehicleWhenNotFoundAndDataIsProvided() {
		Cliente cliente = criarCliente("12345678909");
		Servico servico = criarServico(new BigDecimal("100.00"));
		Veiculo veiculoNovo = criarVeiculoVinculado(cliente.getId());

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.empty());
		when(veiculoRepository.salvar(any())).thenReturn(veiculoNovo);
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoCriacaoOrdemServico resultado = useCase.executar("12345678909", "ABC1D23", "Toyota", "Corolla", 2020,
				List.of(new ItemServicoSolicitado(servico.getId(), 1)), List.of(), null);

		verify(veiculoRepository).salvar(any(Veiculo.class));
		assertNotNull(resultado.veiculo());
	}

	@Test
	void shouldRejectWhenClientNotFound() {
		when(clienteRepository.buscarPorDocumento("00000000000", false)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar("00000000000", "ABC1D23", null, null,
				null, List.of(new ItemServicoSolicitado(UUID.randomUUID(), 1)), List.of(), null));

		verify(ordemServicoRepository, never()).salvar(any());
	}

	@Test
	void shouldRejectWhenVehicleExistsButBelongsToDifferentClient() {
		Cliente cliente = criarCliente("12345678909");
		UUID outroClienteId = UUID.randomUUID();
		Veiculo veiculoDeOutro = criarVeiculoVinculado(outroClienteId);

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculoDeOutro));

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar("12345678909", "ABC1D23", null, null, null,
				List.of(new ItemServicoSolicitado(UUID.randomUUID(), 1)), List.of(), null));
	}

	@Test
	void shouldRejectWhenVehicleNotFoundAndNoDataProvided() {
		Cliente cliente = criarCliente("12345678909");
		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.empty());

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar("12345678909", "ABC1D23", null, null, null,
				List.of(new ItemServicoSolicitado(UUID.randomUUID(), 1)), List.of(), null));
	}

	@Test
	void shouldRejectWhenNoServicesProvided() {
		assertThrows(RegraDeNegocioException.class,
				() -> useCase.executar("12345678909", "ABC1D23", null, null, null, List.of(), List.of(), null));

		assertThrows(RegraDeNegocioException.class,
				() -> useCase.executar("12345678909", "ABC1D23", null, null, null, null, List.of(), null));
	}

	@Test
	void shouldRejectWhenServiceNotFoundInCatalog() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());
		UUID servicoInexistente = UUID.randomUUID();

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servicoInexistente, false)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar("12345678909", "ABC1D23", null, null,
				null, List.of(new ItemServicoSolicitado(servicoInexistente, 1)), List.of(), null));
	}

	@Test
	void shouldRejectWhenStockIsInsufficient() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());
		Servico servico = criarServico(new BigDecimal("100.00"));
		PecaInsumo peca = criarPeca(new BigDecimal("50.00"));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(pecaInsumoRepository.buscarPorId(peca.getId(), false)).thenReturn(Optional.of(peca));
		when(estoqueRepository.calcularQuantidadeTotal(peca.getId())).thenReturn(new BigDecimal("1"));

		RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class,
				() -> useCase.executar("12345678909", "ABC1D23", null, null, null,
						List.of(new ItemServicoSolicitado(servico.getId(), 1)),
						List.of(new ItemPecaSolicitada(peca.getId(), new BigDecimal("5"))), null));

		assertNotNull(ex.getMessage());

		verify(ordemServicoRepository, never()).salvar(any());
	}

	@Test
	void shouldNotifyClientAfterCreation() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());
		Servico servico = criarServico(new BigDecimal("100.00"));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		useCase.executar("12345678909", "ABC1D23", null, null, null,
				List.of(new ItemServicoSolicitado(servico.getId(), 1)), List.of(), null);

		verify(clienteNotificationService).notificarOrcamentoPendente(any(OrdemServico.class), any(Orcamento.class));
	}

	@Test
	void shouldPreserveObservationsOnOs() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());
		Servico servico = criarServico(new BigDecimal("100.00"));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<OrdemServico> captor = ArgumentCaptor.forClass(OrdemServico.class);

		useCase.executar("12345678909", "ABC1D23", null, null, null,
				List.of(new ItemServicoSolicitado(servico.getId(), 1)), List.of(), "Barulho ao frear");

		verify(ordemServicoRepository).salvar(captor.capture());
		assertEquals("Barulho ao frear", captor.getValue().getObservacoes());
	}

	// --- helpers ---

	private Cliente criarCliente(String cpf) {
		return new Cliente(UUID.randomUUID(), "Joao Silva", new Documento(cpf), "joao@email.com", null);
	}

	private Veiculo criarVeiculoVinculado(UUID clienteId) {
		return new Veiculo(UUID.randomUUID(), new Placa("ABC1D23"), "Toyota", "Corolla", 2020, null, null,
				List.of(clienteId));
	}

	private Servico criarServico(BigDecimal valor) {
		return new Servico(UUID.randomUUID(), "Troca de oleo", "Troca completa de oleo", valor, null, null, null, null);
	}

	private PecaInsumo criarPeca(BigDecimal valorUnitario) {
		return new PecaInsumo(UUID.randomUUID(), "OLEO-5W30", "Oleo 5W30", valorUnitario, BigDecimal.ZERO,
				UnidadeMedida.L, TipoItem.INSUMO);
	}

}
