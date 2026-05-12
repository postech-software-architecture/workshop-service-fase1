package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.Orcamento;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de persistencia para o agregado de orcamento.
 */
public interface OrcamentoRepository {

	/**
	 * Persiste o orcamento informado.
	 * @param orcamento agregado a ser persistido.
	 * @return agregado persistido.
	 */
	Orcamento salvar(Orcamento orcamento);

	/**
	 * Busca um orcamento pelo identificador tecnico.
	 * @param id identificador do orcamento.
	 * @return orcamento encontrado, se existir.
	 */
	Optional<Orcamento> buscarPorId(UUID id);

	/**
	 * Lista os orcamentos vinculados a uma ordem de servico.
	 * @param idOrdemServico identificador da ordem de servico.
	 * @return orcamentos vinculados a ordem.
	 */
	List<Orcamento> listarPorOrdemServico(UUID idOrdemServico);

	/**
	 * Verifica se ja existe um orcamento pendente para a ordem informada.
	 * @param idOrdemServico identificador da ordem de servico.
	 * @return {@code true} quando houver orcamento pendente.
	 */
	boolean existePendenteAprovacaoPorOrdemServico(UUID idOrdemServico);

}
