package com.postech.workshop_service.api.controllers;

import com.postech.workshop_service.api.dtos.CadastroClienteRequest;
import com.postech.workshop_service.api.dtos.MovimentacaoRequest;
import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.usecases.AprovarOrcamentoUseCase;
import com.postech.workshop_service.application.usecases.AtualizarClienteUseCase;
import com.postech.workshop_service.application.usecases.AtualizarVeiculoUseCase;
import com.postech.workshop_service.application.usecases.BuscarClientePorDocumentoUseCase;
import com.postech.workshop_service.application.usecases.BuscarClientePorIdUseCase;
import com.postech.workshop_service.application.usecases.BuscarEstoquePorIdUseCase;
import com.postech.workshop_service.application.usecases.BuscarUsuarioAutenticadoUseCase;
import com.postech.workshop_service.application.usecases.BuscarVeiculoPorIdUseCase;
import com.postech.workshop_service.application.usecases.BuscarVeiculoPorPlacaUseCase;
import com.postech.workshop_service.application.usecases.CancelarOrcamentoUseCase;
import com.postech.workshop_service.application.usecases.CriarClienteUseCase;
import com.postech.workshop_service.application.usecases.CriarVeiculoUseCase;
import com.postech.workshop_service.application.usecases.DesvincularClienteVeiculoUseCase;
import com.postech.workshop_service.application.usecases.ListarClientesUseCase;
import com.postech.workshop_service.application.usecases.ListarEstoquesPorPecaUseCase;
import com.postech.workshop_service.application.usecases.ListarMovimentacoesEstoquePorOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.ListarOrcamentosPorOrdemServicoUseCase;
import com.postech.workshop_service.application.usecases.ListarVeiculosPorClienteUseCase;
import com.postech.workshop_service.application.usecases.ListarVeiculosUseCase;
import com.postech.workshop_service.application.usecases.RejeitarOrcamentoUseCase;
import com.postech.workshop_service.application.usecases.RegistrarMovimentacaoUseCase;
import com.postech.workshop_service.application.usecases.RemoverClienteUseCase;
import com.postech.workshop_service.application.usecases.RemoverVeiculoUseCase;
import com.postech.workshop_service.application.usecases.VincularClienteVeiculoUseCase;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.ItemOrcamento;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.StatusOrcamento;
import com.postech.workshop_service.domain.entities.TipoOrcamento;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.valueobjects.Documento;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerCoverageTest {

	@Test
	void deveCobrirBuscaDeClientePorDocumento() {
		BuscarClientePorDocumentoUseCase buscarPorDocumento = mock(BuscarClientePorDocumentoUseCase.class);
		ClienteController controller = new ClienteController(mock(CriarClienteUseCase.class),
				mock(AtualizarClienteUseCase.class), mock(BuscarClientePorIdUseCase.class), buscarPorDocumento,
				mock(ListarClientesUseCase.class), mock(RemoverClienteUseCase.class),
				mock(BuscarUsuarioAutenticadoUseCase.class));
		Cliente cliente = cliente(UUID.randomUUID(), "Cliente");

		when(buscarPorDocumento.executar("98765432100", false)).thenReturn(Optional.of(cliente));
		when(buscarPorDocumento.executar("00000000000", false)).thenReturn(Optional.empty());

		assertThat(controller.buscarPorDocumento("98765432100", false).getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(controller.buscarPorDocumento("00000000000", false).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void deveCobrirCriacaoClienteComEnderecoNulo() {
		CriarClienteUseCase criarCliente = mock(CriarClienteUseCase.class);
		ClienteController controller = new ClienteController(criarCliente, mock(AtualizarClienteUseCase.class),
				mock(BuscarClientePorIdUseCase.class), mock(BuscarClientePorDocumentoUseCase.class),
				mock(ListarClientesUseCase.class), mock(RemoverClienteUseCase.class),
				mock(BuscarUsuarioAutenticadoUseCase.class));
		CadastroClienteRequest request = CadastroClienteRequest.builder()
			.nome("Cliente")
			.documento("98765432100")
			.email("e@e.com")
			.build();
		when(criarCliente.executar("Cliente", "98765432100", "e@e.com", null, null, null, null))
			.thenReturn(cliente(UUID.randomUUID(), "Cliente"));

		assertThat(controller.criar(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
	}

	@Test
	void deveCobrirEstoqueController() {
		RegistrarMovimentacaoUseCase registrar = mock(RegistrarMovimentacaoUseCase.class);
		ListarEstoquesPorPecaUseCase listar = mock(ListarEstoquesPorPecaUseCase.class);
		ListarMovimentacoesEstoquePorOrdemServicoUseCase listarMovimentacoes = mock(
				ListarMovimentacoesEstoquePorOrdemServicoUseCase.class);
		EstoqueController controller = new EstoqueController(mock(BuscarEstoquePorIdUseCase.class), listar,
				listarMovimentacoes, registrar);
		UUID estoqueId = UUID.randomUUID();
		UUID pecaId = UUID.randomUUID();
		UUID ordemServicoId = UUID.randomUUID();
		MovimentacaoRequest request = MovimentacaoRequest.builder()
			.estoqueId(estoqueId)
			.tipo("ENTRADA")
			.quantidade(BigDecimal.ONE)
			.motivo("Entrada")
			.build();
		MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(UUID.randomUUID(), estoqueId,
				TipoMovimentacao.ENTRADA, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, "Entrada",
				LocalDateTime.now(), LocalDateTime.now());
		Estoque estoque = new Estoque(UUID.randomUUID(), pecaId, "A1", BigDecimal.ONE, true, 0, LocalDateTime.now(),
				LocalDateTime.now());
		MovimentacaoEstoque movimentacaoOrdem = new MovimentacaoEstoque(UUID.randomUUID(), estoqueId,
				TipoMovimentacao.RESERVA, BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("9"), "Reserva para OS",
				ordemServicoId, UUID.randomUUID());

		when(registrar.executar(estoqueId, "ENTRADA", BigDecimal.ONE, "Entrada")).thenReturn(movimentacao);
		when(listar.executar(pecaId, false)).thenReturn(List.of(estoque));
		when(listarMovimentacoes.executar(ordemServicoId)).thenReturn(List.of(movimentacaoOrdem));

		assertThat(controller.registrarMovimentacao(request).getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(controller.listarPorPeca(pecaId, false).getBody()).hasSize(1);
		assertThat(controller.listarMovimentacoesPorOrdemServico(ordemServicoId).getBody()).hasSize(1);
	}

	@Test
	void deveCobrirListagemDeOrcamentosPorOrdemServico() {
		ListarOrcamentosPorOrdemServicoUseCase listarOrcamentos = mock(ListarOrcamentosPorOrdemServicoUseCase.class);
		OrcamentoController controller = new OrcamentoController(mock(AprovarOrcamentoUseCase.class),
				mock(RejeitarOrcamentoUseCase.class), mock(CancelarOrcamentoUseCase.class), listarOrcamentos);
		UUID idOrdemServico = UUID.randomUUID();
		Orcamento orcamento = new Orcamento(UUID.randomUUID(), idOrdemServico, new BigDecimal("120.00"),
				List.of(new ItemOrcamento("Troca de oleo", new BigDecimal("120.00"))), TipoOrcamento.SERVICO_ORIGINAL,
				StatusOrcamento.PENDENTE_APROVACAO, LocalDateTime.now(), LocalDateTime.now(), null);

		when(listarOrcamentos.executar(idOrdemServico)).thenReturn(List.of(orcamento));

		var response = controller.listarPorOrdemServico(idOrdemServico);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(1);
		assertThat(response.getBody().get(0).getIdOrdemServico()).isEqualTo(idOrdemServico);
	}

	@Test
	void deveCobrirErrosDeVeiculoController() {
		BuscarVeiculoPorPlacaUseCase buscarPorPlaca = mock(BuscarVeiculoPorPlacaUseCase.class);
		BuscarVeiculoPorIdUseCase buscarPorId = mock(BuscarVeiculoPorIdUseCase.class);
		BuscarClientePorIdUseCase buscarCliente = mock(BuscarClientePorIdUseCase.class);
		VeiculoController controller = new VeiculoController(mock(CriarVeiculoUseCase.class),
				mock(AtualizarVeiculoUseCase.class), buscarPorId, buscarPorPlaca, mock(ListarVeiculosUseCase.class),
				mock(ListarVeiculosPorClienteUseCase.class), mock(RemoverVeiculoUseCase.class),
				mock(VincularClienteVeiculoUseCase.class), mock(DesvincularClienteVeiculoUseCase.class), buscarCliente);
		UUID veiculoId = UUID.randomUUID();
		UUID clienteId = UUID.randomUUID();
		Veiculo veiculo = new Veiculo(veiculoId, "BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of(clienteId),
				true, LocalDateTime.now(), LocalDateTime.now(), null);

		when(buscarPorPlaca.executar("BRA1D23", false)).thenReturn(Optional.empty());
		when(buscarPorId.executar(veiculoId, false)).thenReturn(Optional.of(veiculo));
		when(buscarCliente.executar(clienteId, true)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> controller.buscarPorPlaca("BRA1D23", false))
			.isInstanceOf(RecursoNaoEncontradoException.class);
		assertThatThrownBy(() -> controller.buscarPorId(veiculoId, false))
			.isInstanceOf(RecursoNaoEncontradoException.class);
	}

	@Test
	void deveCobrirHandlersRestantes() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();
		HttpServletRequest request = request("/api/teste");

		assertThat(handler.handleAccessDenied(new AcessoNegadoException("negado"), request).getStatusCode())
			.isEqualTo(HttpStatus.FORBIDDEN);
		assertThat(handler
			.handleMethodNotSupported(
					new HttpRequestMethodNotSupportedException("POST", List.of(HttpMethod.GET.name())), request)
			.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
		assertThat(handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("PATCH"), request)
			.getBody()
			.getMessage()).contains("Métodos permitidos");
		assertThat(handler
			.handleMissingRequestParameter(new MissingServletRequestParameterException("pagina", "int"), request)
			.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
		assertThat(handler.handleDataAccessException(new DataRetrievalFailureException("db"), request).getStatusCode())
			.isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
		assertThat(handler.handleGenericException(new RuntimeException("erro"), request).getStatusCode())
			.isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private Cliente cliente(UUID id, String nome) {
		return new Cliente(id, nome, new Documento("98765432100"), "e@e.com", null);
	}

	private HttpServletRequest request(String uri) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI(uri);
		request.setMethod("GET");
		return request;
	}

}
