package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.Veiculo;

/**
 * Resultado da criacao de uma ordem de servico, agregando todas as entidades geradas
 * atomicamente no fluxo de recepcao.
 *
 * @param ordemServico ordem de servico criada.
 * @param orcamento orcamento pendente de aprovacao gerado automaticamente.
 * @param cliente cliente identificado na recepcao.
 * @param veiculo veiculo recebido na oficina.
 */
public record ResultadoCriacaoOrdemServico(OrdemServico ordemServico, Orcamento orcamento, Cliente cliente,
		Veiculo veiculo) {

}
