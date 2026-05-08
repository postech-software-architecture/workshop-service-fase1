package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.api.dtos.CriarOrdemServicoRequest;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.ItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.ItemOrcamento;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.entities.TipoItemComposicaoTecnica;
import com.postech.workshop_service.domain.entities.TipoOrcamento;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import com.postech.workshop_service.domain.repositories.VeiculoRepository;
import com.postech.workshop_service.domain.valueobjects.Placa;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso responsavel pelo fluxo completo de recepcao de um veiculo e criacao da
 * ordem de servico com orcamento automatico.
 *
 * <p>
 * Orquestra: identificacao do cliente, cadastro/busca do veiculo, validacao de servicos e
 * pecas do catalogo, verificacao de estoque, geracao do numero sequencial da OS, criacao
 * da ordem com itens e geracao do orcamento pendente de aprovacao.
 * </p>
 */
@Service
public class CriarOrdemServicoUseCase {

	private final ClienteRepository clienteRepository;

	private final VeiculoRepository veiculoRepository;

	private final ServicoRepository servicoRepository;

	private final PecaInsumoRepository pecaInsumoRepository;

	private final EstoqueRepository estoqueRepository;

	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	private final OrdemServicoRepository ordemServicoRepository;

	private final OrcamentoRepository orcamentoRepository;

	private final ClienteNotificationService clienteNotificationService;

	private final RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	/**
	 * Construtor para injecao de dependencias.
	 */
	public CriarOrdemServicoUseCase(ClienteRepository clienteRepository, VeiculoRepository veiculoRepository,
			ServicoRepository servicoRepository, PecaInsumoRepository pecaInsumoRepository,
			EstoqueRepository estoqueRepository, MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
			OrdemServicoRepository ordemServicoRepository, OrcamentoRepository orcamentoRepository,
			ClienteNotificationService clienteNotificationService,
			RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase) {
		this.clienteRepository = clienteRepository;
		this.veiculoRepository = veiculoRepository;
		this.servicoRepository = servicoRepository;
		this.pecaInsumoRepository = pecaInsumoRepository;
		this.estoqueRepository = estoqueRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
		this.ordemServicoRepository = ordemServicoRepository;
		this.orcamentoRepository = orcamentoRepository;
		this.clienteNotificationService = clienteNotificationService;
		this.registrarHistoricoUseCase = registrarHistoricoUseCase;
	}

	/**
	 * Executa o fluxo de recepcao e criacao da OS.
	 * @param request dados da recepcao contendo documento do cliente, placa e dados do
	 * veiculo (quando novo), servicos, pecas opcionais e observacoes.
	 * @return resultado com OS, orcamento, cliente e veiculo.
	 */
	@Transactional
	public ResultadoCriacaoOrdemServico executar(CriarOrdemServicoRequest request) {
		try {
			String clienteDocumento = request.getClienteDocumento();
			String veiculoPlaca = request.getVeiculoPlaca();
			String veiculoMarca = request.getVeiculo() != null ? request.getVeiculo().getMarca() : null;
			String veiculoModelo = request.getVeiculo() != null ? request.getVeiculo().getModelo() : null;
			Integer veiculoAno = request.getVeiculo() != null ? request.getVeiculo().getAno() : null;
			String observacoes = request.getObservacoes();

			List<ItemServicoSolicitado> servicos = request.getServicos()
				.stream()
				.map(s -> new ItemServicoSolicitado(s.getServicoId(), s.getQuantidade()))
				.toList();

			List<ItemPecaSolicitada> pecas = request.getPecas() != null ? request.getPecas()
				.stream()
				.map(p -> new ItemPecaSolicitada(p.getPecaId(), p.getQuantidade()))
				.toList() : List.of();

			validarServicos(servicos);

			Cliente cliente = resolverCliente(clienteDocumento);
			Veiculo veiculo = resolverVeiculo(veiculoPlaca, veiculoMarca, veiculoModelo, veiculoAno, cliente);

			List<ItemComposicaoTecnica> itens = new ArrayList<>();
			itens.addAll(construirItensServico(servicos));
			itens.addAll(construirItensPeca(pecas != null ? pecas : List.of()));

			String numero = ordemServicoRepository.gerarProximoNumero(LocalDate.now().getYear());
			OrdemServico os = new OrdemServico(null, cliente.getId(), veiculo.getId(), numero, observacoes, itens);
			StatusOrdemServico statusAnterior = os.getStatus();
			os.encerrarComposicao();

			List<ItemOrcamento> itensOrcamento = os.getItensComposicao()
				.stream()
				.map(i -> new ItemOrcamento(i.getDescricao(), i.getValor()))
				.toList();
			BigDecimal valorTotal = itensOrcamento.stream()
				.map(ItemOrcamento::getValor)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

			Orcamento orcamento = new Orcamento(null, os.getId(), valorTotal, itensOrcamento,
					TipoOrcamento.SERVICO_ORIGINAL);
			orcamento.enviarParaAprovacao();

			OrdemServico osSalva = ordemServicoRepository.salvar(os);
			registrarHistoricoUseCase.executar(osSalva.getId(), statusAnterior, osSalva.getStatus());
			Orcamento orcamentoSalvo = orcamentoRepository.salvar(orcamento);

			reservarEstoque(pecas != null ? pecas : List.of(), osSalva, orcamentoSalvo);

			clienteNotificationService.notificarOrcamentoPendente(osSalva, orcamentoSalvo);

			return new ResultadoCriacaoOrdemServico(osSalva, orcamentoSalvo, cliente, veiculo);
		}
		catch (RegraDeNegocioException | RecursoNaoEncontradoException ex) {
			throw ex;
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}
		catch (ObjectOptimisticLockingFailureException ex) {
			throw new RegraDeNegocioException(
					"Nao foi possivel criar a ordem de servico porque o estoque foi alterado por outro usuario simultaneamente. Tente novamente.");
		}
	}

	private void reservarEstoque(List<ItemPecaSolicitada> pecas, OrdemServico os, Orcamento orcamento) {
		String motivo = "Reserva para OS " + os.getNumero();
		for (ItemPecaSolicitada p : pecas) {
			BigDecimal quantidadeRestante = p.quantidade();
			if (quantidadeRestante == null || quantidadeRestante.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}

			List<Estoque> estoques = estoqueRepository.listarPorPeca(p.pecaId(), false)
				.stream()
				.filter(e -> e.isAtivo() && e.getQuantidade().compareTo(BigDecimal.ZERO) > 0)
				.sorted((e1, e2) -> e2.getQuantidade().compareTo(e1.getQuantidade()))
				.toList();

			for (Estoque estoque : estoques) {
				if (quantidadeRestante.compareTo(BigDecimal.ZERO) <= 0) {
					break;
				}
				BigDecimal quantidadeReserva = quantidadeRestante.min(estoque.getQuantidade());
				MovimentacaoEstoque mov = estoque.reservar(quantidadeReserva, motivo, os.getId(), orcamento.getId());
				estoqueRepository.salvar(estoque);
				movimentacaoEstoqueRepository.salvar(mov);
				quantidadeRestante = quantidadeRestante.subtract(quantidadeReserva);
			}

			if (quantidadeRestante.compareTo(BigDecimal.ZERO) > 0) {
				throw new RegraDeNegocioException(
						"Estoque insuficiente para reservar a quantidade solicitada da peca.");
			}
		}
	}

	private void validarServicos(List<ItemServicoSolicitado> servicos) {
		if (servicos == null || servicos.isEmpty()) {
			throw new RegraDeNegocioException("A ordem de servico deve ter pelo menos um servico selecionado.");
		}
		for (ItemServicoSolicitado item : servicos) {
			if (item.quantidade() <= 0) {
				throw new RegraDeNegocioException("A quantidade de cada servico deve ser maior que zero.");
			}
		}
	}

	private Cliente resolverCliente(String documento) {
		String documentoLimpo = documento.replaceAll("[^0-9]", "");
		return clienteRepository.buscarPorDocumento(documentoLimpo, false)
			.orElseThrow(() -> new RecursoNaoEncontradoException(
					"Cliente nao encontrado para o documento informado. Realize o cadastro antes de abrir uma OS."));
	}

	private Veiculo resolverVeiculo(String placaRaw, String marca, String modelo, Integer ano, Cliente cliente) {
		Placa placa;
		try {
			placa = new Placa(placaRaw);
		}
		catch (IllegalArgumentException ex) {
			throw new RegraDeNegocioException(ex.getMessage());
		}

		return veiculoRepository.buscarPorPlaca(placa.getValor(), false).map(v -> {
			if (!v.getClientesVinculados().contains(cliente.getId())) {
				throw new RegraDeNegocioException(
						"O veiculo informado nao pertence ao cliente. Verifique a placa ou o cliente.");
			}
			return v;
		}).orElseGet(() -> cadastrarNovoVeiculo(placa, marca, modelo, ano, cliente));
	}

	private Veiculo cadastrarNovoVeiculo(Placa placa, String marca, String modelo, Integer ano, Cliente cliente) {
		if (marca == null || modelo == null || ano == null) {
			throw new RegraDeNegocioException(
					"Veiculo nao encontrado pela placa informada. Informe marca, modelo e ano para cadastra-lo.");
		}
		Veiculo novo = new Veiculo(placa.getValor(), marca, modelo, ano, null, null, List.of(cliente.getId()));
		return veiculoRepository.salvar(novo);
	}

	private List<ItemComposicaoTecnica> construirItensServico(List<ItemServicoSolicitado> solicitados) {
		List<ItemComposicaoTecnica> itens = new ArrayList<>();
		for (ItemServicoSolicitado s : solicitados) {
			Servico servico = servicoRepository.buscarPorId(s.servicoId(), false)
				.orElseThrow(() -> new RecursoNaoEncontradoException(
						"Servico nao encontrado: " + s.servicoId() + ". Verifique o catalogo."));
			if (!servico.isAtivo()) {
				throw new RegraDeNegocioException("O servico '" + servico.getNome() + "' esta inativo.");
			}
			BigDecimal valorTotal = servico.getValor().multiply(BigDecimal.valueOf(s.quantidade()));
			itens.add(new ItemComposicaoTecnica(servico.getNome(), valorTotal, TipoItemComposicaoTecnica.SERVICO));
		}
		return itens;
	}

	private List<ItemComposicaoTecnica> construirItensPeca(List<ItemPecaSolicitada> solicitadas) {
		List<ItemComposicaoTecnica> itens = new ArrayList<>();
		for (ItemPecaSolicitada p : solicitadas) {
			if (p.quantidade() == null || p.quantidade().compareTo(BigDecimal.ZERO) <= 0) {
				throw new RegraDeNegocioException("A quantidade de cada peca deve ser maior que zero.");
			}
			PecaInsumo peca = pecaInsumoRepository.buscarPorId(p.pecaId(), false)
				.orElseThrow(() -> new RecursoNaoEncontradoException(
						"Peca nao encontrada: " + p.pecaId() + ". Verifique o catalogo."));
			if (!peca.isAtivo()) {
				throw new RegraDeNegocioException("A peca '" + peca.getNome() + "' esta inativa.");
			}
			BigDecimal estoqueDisponivel = estoqueRepository.calcularQuantidadeTotal(peca.getId());
			if (estoqueDisponivel.compareTo(p.quantidade()) < 0) {
				throw new RegraDeNegocioException("Estoque insuficiente para '" + peca.getNome() + "'. Disponivel: "
						+ estoqueDisponivel + ", solicitado: " + p.quantidade() + ".");
			}
			BigDecimal valorTotal = peca.getValorUnitario().multiply(p.quantidade());
			TipoItemComposicaoTecnica tipoItem = peca.getTipoItem() == TipoItem.INSUMO
					? TipoItemComposicaoTecnica.INSUMO : TipoItemComposicaoTecnica.PECA;
			itens.add(new ItemComposicaoTecnica(peca.getNome(), valorTotal, tipoItem, peca.getId()));
		}
		return itens;
	}

}
