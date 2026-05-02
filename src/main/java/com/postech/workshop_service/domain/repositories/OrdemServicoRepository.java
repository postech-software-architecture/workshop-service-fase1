package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.OrdemServico;

import java.util.Optional;
import java.util.UUID;

/**
 * Contrato de persistencia para o agregado de ordem de servico.
 */
public interface OrdemServicoRepository {

	/**
	 * Persiste a ordem de servico informada.
	 * @param ordemServico agregado a ser persistido.
	 * @return agregado persistido.
	 */
	OrdemServico salvar(OrdemServico ordemServico);

	/**
	 * Busca uma ordem de servico pelo identificador tecnico.
	 * @param id identificador da ordem.
	 * @return ordem encontrada, se existir.
	 */
	Optional<OrdemServico> buscarPorId(UUID id);

	/**
	 * Gera o proximo numero sequencial disponivel para o ano informado.
	 * @param ano ano de referencia (ex: 2026).
	 * @return numero formatado no padrao OS-{ANO}-{NNNNN}.
	 */
	String gerarProximoNumero(int ano);

}
