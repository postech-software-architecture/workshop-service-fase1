package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.repositories.MetricaExecucaoRepository;
import com.postech.workshop_service.domain.valueobjects.TempoMedioGlobal;
import com.postech.workshop_service.domain.valueobjects.TempoMedioPorTipoServico;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementacao de metricas de execucao usando consultas SQL de agregacao.
 */
@Repository
public class MetricaExecucaoRepositoryImpl implements MetricaExecucaoRepository {

	private static final String SQL_TEMPO_MEDIO_GLOBAL = """
			SELECT
			    COUNT(*)                                                                          AS total_ordens,
			    COALESCE(AVG(EXTRACT(EPOCH FROM (data_finalizacao - data_inicio_execucao)) / 60), 0) AS tempo_medio,
			    COALESCE(MIN(EXTRACT(EPOCH FROM (data_finalizacao - data_inicio_execucao)) / 60), 0) AS tempo_minimo,
			    COALESCE(MAX(EXTRACT(EPOCH FROM (data_finalizacao - data_inicio_execucao)) / 60), 0) AS tempo_maximo
			FROM ordens_servico
			WHERE status IN ('FINALIZADA', 'ENTREGUE')
			  AND data_inicio_execucao IS NOT NULL
			  AND data_finalizacao IS NOT NULL
			""";

	private static final String SQL_TEMPO_MEDIO_POR_TIPO_SERVICO = """
			SELECT
			    i.descricao                                                                           AS descricao_servico,
			    COUNT(DISTINCT os.id)                                                                 AS total_execucoes,
			    AVG(EXTRACT(EPOCH FROM (os.data_finalizacao - os.data_inicio_execucao)) / 60)         AS tempo_medio
			FROM ordens_servico os
			JOIN ordens_servico_itens i ON i.ordem_servico_id = os.id
			WHERE os.status IN ('FINALIZADA', 'ENTREGUE')
			  AND os.data_inicio_execucao IS NOT NULL
			  AND os.data_finalizacao IS NOT NULL
			  AND i.tipo = 'SERVICO'
			GROUP BY i.descricao
			ORDER BY tempo_medio DESC
			""";

	private final JdbcTemplate jdbcTemplate;

	public MetricaExecucaoRepositoryImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public TempoMedioGlobal consultarTempoMedioGlobal() {
		return jdbcTemplate.queryForObject(SQL_TEMPO_MEDIO_GLOBAL,
				(rs, rowNum) -> new TempoMedioGlobal(rs.getLong("total_ordens"), rs.getDouble("tempo_medio"),
						rs.getDouble("tempo_minimo"), rs.getDouble("tempo_maximo")));
	}

	@Override
	public List<TempoMedioPorTipoServico> consultarTempoMedioPorTipoServico() {
		return jdbcTemplate.query(SQL_TEMPO_MEDIO_POR_TIPO_SERVICO,
				(rs, rowNum) -> new TempoMedioPorTipoServico(rs.getString("descricao_servico"),
						rs.getLong("total_execucoes"), rs.getDouble("tempo_medio")));
	}

}
