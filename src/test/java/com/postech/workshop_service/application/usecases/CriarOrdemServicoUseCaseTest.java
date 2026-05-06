package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.entities.StatusOrcamento;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
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
	private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

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

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("123.456.789-09");
		request.setVeiculoPlaca("ABC1D23");
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(servico.getId(), 1)));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoCriacaoOrdemServico resultado = useCase.executar(request);

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
		Estoque estoque = criarEstoque(peca.getId(), new BigDecimal("10"));

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("12345678909");
		request.setVeiculoPlaca("ABC1D23");
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(servico.getId(), 1)));
		request.setPecas(List.of(new CriarOrdemServicoRequest.ItemPecaRequest(peca.getId(), new BigDecimal("2"))));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(pecaInsumoRepository.buscarPorId(peca.getId(), false)).thenReturn(Optional.of(peca));
		when(estoqueRepository.calcularQuantidadeTotal(peca.getId())).thenReturn(new BigDecimal("10"));
		when(estoqueRepository.listarPorPeca(peca.getId(), false)).thenReturn(List.of(estoque));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoCriacaoOrdemServico resultado = useCase.executar(request);

		// R$80 servico + R$50*2 pecas = R$180
		assertEquals(new BigDecimal("180.00"), resultado.orcamento().getValor());
	}

	@Test
	void shouldReserveStockWhenCreatingOsWithParts() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());
		Servico servico = criarServico(new BigDecimal("100.00"));
		PecaInsumo peca = criarPeca(new BigDecimal("50.00"));
		Estoque estoque = criarEstoque(peca.getId(), new BigDecimal("10"));

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("12345678909");
		request.setVeiculoPlaca("ABC1D23");
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(servico.getId(), 1)));
		request.setPecas(List.of(new CriarOrdemServicoRequest.ItemPecaRequest(peca.getId(), new BigDecimal("2"))));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(pecaInsumoRepository.buscarPorId(peca.getId(), false)).thenReturn(Optional.of(peca));
		when(estoqueRepository.calcularQuantidadeTotal(peca.getId())).thenReturn(new BigDecimal("10"));
		when(estoqueRepository.listarPorPeca(peca.getId(), false)).thenReturn(List.of(estoque));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		useCase.executar(request);

		verify(estoqueRepository).listarPorPeca(peca.getId(), false);
		verify(movimentacaoEstoqueRepository).salvar(any());
	}

	@Test
	void shouldCreateVehicleWhenNotFoundAndDataIsProvided() {
		Cliente cliente = criarCliente("12345678909");
		Servico servico = criarServico(new BigDecimal("100.00"));
		Veiculo veiculoNovo = criarVeiculoVinculado(cliente.getId());

		CriarOrdemServicoRequest.DadosVeiculoRequest dadosVeiculoRequest = new CriarOrdemServicoRequest.DadosVeiculoRequest();
		dadosVeiculoRequest.setMarca("Toyota");
		dadosVeiculoRequest.setModelo("Corolla");
		dadosVeiculoRequest.setAno(2020);

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("12345678909");
		request.setVeiculoPlaca("ABC1D23");
		request.setVeiculo(dadosVeiculoRequest);
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(servico.getId(), 1)));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.empty());
		when(veiculoRepository.salvar(any())).thenReturn(veiculoNovo);
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ResultadoCriacaoOrdemServico resultado = useCase.executar(request);

		verify(veiculoRepository).salvar(any(Veiculo.class));
		assertNotNull(resultado.veiculo());
	}

	@Test
	void shouldRejectWhenClientNotFound() {
		when(clienteRepository.buscarPorDocumento("00000000000", false)).thenReturn(Optional.empty());

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("00000000000");
		request.setVeiculoPlaca("ABC1D23");
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(UUID.randomUUID(), 1)));

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(request));

		verify(ordemServicoRepository, never()).salvar(any());
	}

	@Test
	void shouldRejectWhenVehicleExistsButBelongsToDifferentClient() {
		Cliente cliente = criarCliente("12345678909");
		UUID outroClienteId = UUID.randomUUID();
		Veiculo veiculoDeOutro = criarVeiculoVinculado(outroClienteId);

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("12345678909");
		request.setVeiculoPlaca("ABC1D23");
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(UUID.randomUUID(), 1)));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculoDeOutro));

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(request));
	}

	@Test
	void shouldRejectWhenVehicleNotFoundAndNoDataProvided() {
		Cliente cliente = criarCliente("12345678909");
		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.empty());

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("12345678909");
		request.setVeiculoPlaca("ABC1D23");
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(UUID.randomUUID(), 1)));

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(request));
	}

	@Test
	void shouldRejectWhenNoServicesProvided() {
		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("12345678909");
		request.setVeiculoPlaca("ABC1D23");
		request.setPecas(List.of());
		request.setServicos(List.of());

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(request));
	}

	@Test
	void shouldRejectWhenServiceNotFoundInCatalog() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("12345678909");
		request.setVeiculoPlaca("ABC1D23");
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(UUID.randomUUID(), 1)));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(any(UUID.class), anyBoolean())).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(request));

		verify(ordemServicoRepository, never()).salvar(any());
	}

	@Test
	void shouldRejectWhenStockIsInsufficient() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());
		Servico servico = criarServico(new BigDecimal("100.00"));
		PecaInsumo peca = criarPeca(new BigDecimal("50.00"));

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("12345678909");
		request.setVeiculoPlaca("ABC1D23");
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(servico.getId(), 1)));
		request.setPecas(List.of(new CriarOrdemServicoRequest.ItemPecaRequest(peca.getId(), new BigDecimal("50.00"))));

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(pecaInsumoRepository.buscarPorId(peca.getId(), false)).thenReturn(Optional.of(peca));
		when(estoqueRepository.calcularQuantidadeTotal(peca.getId())).thenReturn(new BigDecimal("1"));

		RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () -> useCase.executar(request));

		assertNotNull(ex.getMessage());

		verify(ordemServicoRepository, never()).salvar(any());
	}

	@Test
	void shouldNotifyClientAfterCreation() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());
		Servico servico = criarServico(new BigDecimal("100.00"));

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("12345678909");
		request.setVeiculoPlaca("ABC1D23");
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(servico.getId(), 1)));
		request.setPecas(List.of());

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		useCase.executar(request);

		verify(clienteNotificationService).notificarOrcamentoPendente(any(OrdemServico.class), any(Orcamento.class));
	}

	@Test
	void shouldPreserveObservationsOnOs() {
		Cliente cliente = criarCliente("12345678909");
		Veiculo veiculo = criarVeiculoVinculado(cliente.getId());
		Servico servico = criarServico(new BigDecimal("100.00"));

		CriarOrdemServicoRequest request = new CriarOrdemServicoRequest();
		request.setClienteDocumento("12345678909");
		request.setVeiculoPlaca("ABC1D23");
		request.setServicos(List.of(new CriarOrdemServicoRequest.ItemServicoRequest(servico.getId(), 1)));
		request.setPecas(List.of());
		request.setObservacoes("Barulho ao frear");

		when(clienteRepository.buscarPorDocumento("12345678909", false)).thenReturn(Optional.of(cliente));
		when(veiculoRepository.buscarPorPlaca(anyString(), anyBoolean())).thenReturn(Optional.of(veiculo));
		when(servicoRepository.buscarPorId(servico.getId(), false)).thenReturn(Optional.of(servico));
		when(ordemServicoRepository.gerarProximoNumero(anyInt())).thenReturn("OS-2026-00001");
		when(ordemServicoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));
		when(orcamentoRepository.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<OrdemServico> captor = ArgumentCaptor.forClass(OrdemServico.class);

		useCase.executar(request);

		verify(ordemServicoRepository).salvar(captor.capture());
		assertEquals("Barulho ao frear", captor.getValue().getObservacoes());
	}

	// --- helpers ---

	private Cliente criarCliente(String cpf) {
		return new Cliente(UUID.randomUUID(), "Joao Silva", new Documento(cpf), "joao@email.com", null);
	}

	private Veiculo criarVeiculoVinculado(UUID clienteId) {
		return new Veiculo(UUID.randomUUID(), "ABC1D23", "Toyota", "Corolla", 2020, null, null,
				List.of(clienteId), true, LocalDateTime.now(), LocalDateTime.now(), null);
	}

	private Servico criarServico(BigDecimal valor) {
		return new Servico(UUID.randomUUID(), "Troca de oleo", "Troca completa de oleo", valor, null, null, null, null);
	}

	private PecaInsumo criarPeca(BigDecimal valorUnitario) {
		return new PecaInsumo(UUID.randomUUID(), "OLEO-5W30", "Oleo 5W30", valorUnitario, BigDecimal.ZERO,
				UnidadeMedida.L, TipoItem.INSUMO);
	}

	private Estoque criarEstoque(UUID pecaId, BigDecimal quantidade) {
		return new Estoque(UUID.randomUUID(), pecaId, "Prateleira A", quantidade);
	}

}
