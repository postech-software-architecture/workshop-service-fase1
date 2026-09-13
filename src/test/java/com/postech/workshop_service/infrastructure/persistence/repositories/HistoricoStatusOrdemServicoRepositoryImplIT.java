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
		UUID usuarioId = inserirUsuario();
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

	// As FKs da W3 exigem cliente e veiculo existentes, e a limpeza entre testes trunca
	// ambas as tabelas; por isso a fixture cria as duas linhas antes da ordem.
	private UUID inserirOrdemServico() {
		UUID ordemId = UUID.randomUUID();
		UUID clienteId = UUID.randomUUID();
		UUID veiculoId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();

		jdbcTemplate.update(
				"INSERT INTO clientes (id, nome, documento, email, data_criacao, data_ultima_atualizacao, ativo) VALUES (?, ?, ?, ?, ?, ?, true)",
				clienteId, "Cliente Fixture", documentoFixture(), "fixture@teste.com", agora, agora);
		jdbcTemplate.update(
				"INSERT INTO veiculos (id, placa, marca, modelo, ano, ativo, data_criacao, data_ultima_atualizacao) VALUES (?, ?, ?, ?, ?, true, ?, ?)",
				veiculoId, placaFixture(), "Toyota", "Corolla", 2020, agora, agora);
		jdbcTemplate.update(
				"INSERT INTO ordens_servico (id, id_cliente, id_veiculo, status, numero, data_criacao, data_ultima_atualizacao) VALUES (?, ?, ?, ?, ?, ?, ?)",
				ordemId, clienteId, veiculoId, StatusOrdemServico.AGUARDANDO_EXECUCAO.name(),
				"OS-2026-" + ordemId.toString().substring(0, 5), agora, agora);
		return ordemId;
	}

	// fk_historico_status_os_usuarios (W3) exige um responsavel existente.
	private UUID inserirUsuario() {
		UUID usuarioId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		jdbcTemplate.update(
				"INSERT INTO usuarios (id, username, email, senha_hash, cliente_id, ativo, bloqueado, data_criacao, data_ultima_atualizacao) VALUES (?, ?, ?, 'hash', NULL, true, false, ?, ?)",
				usuarioId, "mecanico." + usuarioId.toString().substring(0, 8), usuarioId + "@teste.com", agora, agora);
		return usuarioId;
	}

	private String documentoFixture() {
		return String.format("%011d", Math.floorMod(UUID.randomUUID().getMostSignificantBits(), 100_000_000_000L));
	}

	private String placaFixture() {
		String sufixo = UUID.randomUUID().toString().replaceAll("[^0-9]", "");
		return "FIX" + sufixo.substring(0, 4);
	}

}
