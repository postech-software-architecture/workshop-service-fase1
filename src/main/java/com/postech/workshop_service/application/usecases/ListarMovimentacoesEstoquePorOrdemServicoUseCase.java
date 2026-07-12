package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Caso de uso responsavel por listar as movimentacoes de estoque vinculadas a uma ordem
 * de servico.
 */
@Service
public class ListarMovimentacoesEstoquePorOrdemServicoUseCase {

	private final OrdemServicoRepository ordemServicoRepository;

	private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	/**
	 * Construtor para injecao de dependencias.
	 * @param ordemServicoRepository repositorio de ordens.
	 * @param movimentacaoEstoqueRepository repositorio de movimentacoes.
	 */
	public ListarMovimentacoesEstoquePorOrdemServicoUseCase(OrdemServicoRepository ordemServicoRepository,
			MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
		this.ordemServicoRepository = ordemServicoRepository;
		this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
	}

	/**
	 * Lista as movimentacoes de estoque de uma ordem existente.
	 * @param ordemServicoId identificador da ordem de servico.
	 * @return lista de movimentacoes da ordem.
	 */
	@Transactional(readOnly = true)
	public List<MovimentacaoEstoque> executar(UUID ordemServicoId) {
		ordemServicoRepository.buscarPorId(ordemServicoId)
			.orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de servico nao encontrada."));
		return movimentacaoEstoqueRepository.listarPorOrdemServico(ordemServicoId);
	}

}
