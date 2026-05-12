package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por listar os orcamentos vinculados a uma ordem de servico.
 */
@Service
public class ListarOrcamentosPorOrdemServicoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	private final OrcamentoRepository orcamentoRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param ordemServicoRepository repositorio de ordens de servico.
	 * @param orcamentoRepository repositorio de orcamentos.
	 */
	public ListarOrcamentosPorOrdemServicoUseCase(OrdemServicoRepository ordemServicoRepository,
			OrcamentoRepository orcamentoRepository) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.orcamentoRepository = orcamentoRepository;
	}

	/**
	 * Lista os orcamentos de uma ordem existente.
	 * @param idOrdemServico identificador da ordem de servico.
	 * @return lista de orcamentos vinculados a ordem.
	 */
	@Transactional(readOnly = true)
	public List<Orcamento> executar(UUID idOrdemServico) {
		ordemServicoRepository.buscarPorId(idOrdemServico)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		return orcamentoRepository.listarPorOrdemServico(idOrdemServico);
	}

}
