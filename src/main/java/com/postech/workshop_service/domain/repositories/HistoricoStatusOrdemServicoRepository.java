package com.postech.workshop_service.domain.repositories;

import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;

import java.util.List;
import java.util.UUID;

/**
 * Contrato de persistencia para historico de status da ordem de servico.
 */
public interface HistoricoStatusOrdemServicoRepository {

	HistoricoStatusOrdemServico salvar(HistoricoStatusOrdemServico historico);

	List<HistoricoStatusOrdemServico> listarPorOrdemServico(UUID idOrdemServico);

}
