package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de persistencia para movimentacoes de estoque.
 */
public interface MovimentacaoEstoqueRepository {

	/**
	 * Persiste uma movimentacao no repositorio.
	 * @param movimentacao entidade a ser persistida.
	 * @return entidade persistida.
	 */
	MovimentacaoEstoque salvar(MovimentacaoEstoque movimentacao);

	/**
	 * Busca uma movimentacao pelo seu identificador tecnico.
	 * @param id identificador da movimentacao.
	 * @return movimentacao encontrada, se existir.
	 */
	Optional<MovimentacaoEstoque> buscarPorId(UUID id);

	/**
	 * Lista o historico de movimentacoes de um estoque.
	 * @param estoqueId identificador do estoque.
	 * @param tipo filtro por tipo de movimentacao (opcional).
	 * @param dataInicio filtro por data inicial (opcional).
	 * @param dataFim filtro por data final (opcional).
	 * @return lista de movimentacoes ordenadas por data decrescente.
	 */
	List<MovimentacaoEstoque> listarPorEstoque(UUID estoqueId, TipoMovimentacao tipo, LocalDateTime dataInicio,
			LocalDateTime dataFim);

	/**
	 * Lista todas as movimentacoes de uma peca (todos os estoques).
	 * @param pecaInsumoId identificador da peca.
	 * @param tipo filtro por tipo de movimentacao (opcional).
	 * @param dataInicio filtro por data inicial (opcional).
	 * @param dataFim filtro por data final (opcional).
	 * @return lista de movimentacoes ordenadas por data decrescente.
	 */
	List<MovimentacaoEstoque> listarPorPeca(UUID pecaInsumoId, TipoMovimentacao tipo, LocalDateTime dataInicio,
			LocalDateTime dataFim);

	/**
	 * Lista todas as movimentacoes vinculadas a uma ordem de servico.
	 * @param ordemServicoId identificador da ordem de servico.
	 * @return lista de movimentacoes ordenadas por data decrescente.
	 */
	List<MovimentacaoEstoque> listarPorOrdemServico(UUID ordemServicoId);

}
