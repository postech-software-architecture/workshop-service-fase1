package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.config.PostgresTestContainer;
import com.postech.workshop_service.domain.entities.HistoricoStatusOrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.HistoricoStatusOrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricoStatusOrdemServicoRepositoryImplIT extends PostgresTestContainer {

	@Autowired
	private HistoricoStatusOrdemServicoRepository historicoRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void shouldSaveAndListHistoryChronologically() {
		UUID ordemId = inserirOrdemServico();
		UUID usuarioId = UUID.randomUUID();
		HistoricoStatusOrdemServico segundo = new HistoricoStatusOrdemServico(null, ordemId,
				StatusOrdemServico.AGUARDANDO_EXECUCAO, StatusOrdemServico.EM_EXECUCAO,
				LocalDateTime.now().plusMinutes(1), usuarioId, "mecanico");
		HistoricoStatusOrdemServico primeiro = new HistoricoStatusOrdemServico(null, ordemId,
				StatusOrdemServico.AGUARDANDO_APROVACAO, StatusOrdemServico.AGUARDANDO_EXECUCAO, LocalDateTime.now(),
				usuarioId, "mecanico");

		historicoRepository.salvar(segundo);
		historicoRepository.salvar(primeiro);

		List<HistoricoStatusOrdemServico> historico = historicoRepository.listarPorOrdemServico(ordemId);

		assertThat(historico).hasSize(2);
		assertThat(historico.get(0).getStatusNovo()).isEqualTo(StatusOrdemServico.AGUARDANDO_EXECUCAO);
		assertThat(historico.get(1).getStatusNovo()).isEqualTo(StatusOrdemServico.EM_EXECUCAO);
	}

	@Test
	void shouldNotBackfillHistoryForExistingOrder() {
		UUID ordemId = inserirOrdemServico();

		List<HistoricoStatusOrdemServico> historico = historicoRepository.listarPorOrdemServico(ordemId);

		assertThat(historico).isEmpty();
	}

	private UUID inserirOrdemServico() {
		UUID ordemId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		jdbcTemplate.update(
				"INSERT INTO ordens_servico (id, id_cliente, id_veiculo, status, numero, data_criacao, data_ultima_atualizacao) VALUES (?, ?, ?, ?, ?, ?, ?)",
				ordemId, UUID.randomUUID(), UUID.randomUUID(), StatusOrdemServico.AGUARDANDO_EXECUCAO.name(),
				"OS-2026-" + ordemId.toString().substring(0, 5), agora, agora);
		return ordemId;
	}

}
