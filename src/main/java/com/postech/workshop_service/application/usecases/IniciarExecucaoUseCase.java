package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por iniciar a execucao tecnica de uma ordem aprovada.
 */
@Service
public class IniciarExecucaoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	private final EstoqueRepository estoqueRepository;

	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	private final RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase;

	public IniciarExecucaoUseCase(OrdemServicoRepository ordemServicoRepository, EstoqueRepository estoqueRepository,
			MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
			RegistrarHistoricoStatusOrdemServicoUseCase registrarHistoricoUseCase) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.estoqueRepository = estoqueRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
		this.registrarHistoricoUseCase = registrarHistoricoUseCase;
	}

	@Transactional
	public OrdemServico executar(UUID idOrdemServico) {
		OrdemServico ordemServico = ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		List<MovimentacaoEstoque> reservas = movimentacaoEstoqueRepository.listarPorOrdemServico(idOrdemServico)
			.stream()
			.filter(mov -> mov.getTipo() == TipoMovimentacao.RESERVA)
			.toList();
		consumirReservas(ordemServico, reservas);
		StatusOrdemServico statusAnterior = ordemServico.getStatus();
		ordemServico.iniciarExecucao();
		OrdemServico ordemSalva = ordemServicoRepository.salvar(ordemServico);
		registrarHistoricoUseCase.executar(ordemSalva.getId(), statusAnterior, ordemSalva.getStatus());
		return ordemSalva;
	}

	private void consumirReservas(OrdemServico ordemServico, List<MovimentacaoEstoque> reservas) {
		String motivo = "Consumo de reserva OS " + ordemServico.getNumero();
		for (MovimentacaoEstoque reserva : reservas) {
			Estoque estoque = estoqueRepository.buscarPorId(reserva.getEstoqueId(), true)
				.orElseThrow(
						() -> new RecursoNaoEncontradoException("Estoque nao encontrado para consumo da reserva."));
			MovimentacaoEstoque saida = estoque.consumirReserva(reserva.getQuantidade(), motivo, ordemServico.getId(),
					reserva.getOrcamentoId());
			movimentacaoEstoqueRepository.salvar(saida);
		}
	}

}
