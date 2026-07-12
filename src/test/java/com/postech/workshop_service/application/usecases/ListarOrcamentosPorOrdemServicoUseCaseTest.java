package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListarOrcamentosPorOrdemServicoUseCaseTest {

	private final OrdemServicoRepository ordemServicoRepository = mock(OrdemServicoRepository.class);

	private final OrcamentoRepository orcamentoRepository = mock(OrcamentoRepository.class);

	private final ListarOrcamentosPorOrdemServicoUseCase useCase = new ListarOrcamentosPorOrdemServicoUseCase(
			ordemServicoRepository, orcamentoRepository);

	@Test
	void deveListarOrcamentosQuandoOrdemExiste() {
		UUID idOrdemServico = UUID.randomUUID();
		Orcamento orcamento = mock(Orcamento.class);

		when(ordemServicoRepository.buscarPorId(idOrdemServico)).thenReturn(Optional.of(mock(OrdemServico.class)));
		when(orcamentoRepository.listarPorOrdemServico(idOrdemServico)).thenReturn(List.of(orcamento));

		List<Orcamento> resultado = useCase.executar(idOrdemServico);

		assertThat(resultado).containsExactly(orcamento);
	}

	@Test
	void deveFalharQuandoOrdemNaoExiste() {
		UUID idOrdemServico = UUID.randomUUID();
		when(ordemServicoRepository.buscarPorId(idOrdemServico)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> useCase.executar(idOrdemServico)).isInstanceOf(RecursoNaoEncontradoException.class)
			.hasMessage("Ordem de servico nao encontrada.");
		verify(orcamentoRepository, never()).listarPorOrdemServico(idOrdemServico);
	}

}
