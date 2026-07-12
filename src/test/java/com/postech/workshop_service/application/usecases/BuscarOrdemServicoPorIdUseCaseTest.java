package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.ItemOrcamento;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrcamento;
import com.postech.workshop_service.domain.entities.TipoOrcamento;
import com.postech.workshop_service.domain.repositories.OrcamentoRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarOrdemServicoPorIdUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private OrcamentoRepository orcamentoRepository;

	@InjectMocks
	private BuscarOrdemServicoPorIdUseCase useCase;

	@Test
	void deveRetornarOrdemQuandoEncontrada() {
		UUID id = UUID.randomUUID();
		OrdemServico ordem = new OrdemServico(id, UUID.randomUUID(), UUID.randomUUID());
		when(ordemServicoRepository.buscarPorId(id)).thenReturn(Optional.of(ordem));
		when(orcamentoRepository.listarPorOrdemServico(id)).thenReturn(List.of());

		OrdemServico resultado = useCase.executar(id);

		assertThat(resultado).isSameAs(ordem);
		assertThat(resultado.getOrcamentoAtual()).isNull();
	}

	@Test
	void deveVincularOrcamentoAtualQuandoExistir() {
		UUID id = UUID.randomUUID();
		OrdemServico ordem = new OrdemServico(id, UUID.randomUUID(), UUID.randomUUID());
		Orcamento orcamento = criarOrcamento(id);
		when(ordemServicoRepository.buscarPorId(id)).thenReturn(Optional.of(ordem));
		when(orcamentoRepository.listarPorOrdemServico(id)).thenReturn(List.of(orcamento));

		OrdemServico resultado = useCase.executar(id);

		assertThat(resultado.getOrcamentoAtual()).isSameAs(orcamento);
	}

	@Test
	void deveLancarRecursoNaoEncontradoQuandoOrdemAusente() {
		UUID id = UUID.randomUUID();
		when(ordemServicoRepository.buscarPorId(id)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(id));
		verify(orcamentoRepository, never()).listarPorOrdemServico(id);
	}

	private Orcamento criarOrcamento(UUID idOrdemServico) {
		return new Orcamento(UUID.randomUUID(), idOrdemServico, new BigDecimal("120.00"),
				List.of(new ItemOrcamento("Troca de oleo", new BigDecimal("120.00"))), TipoOrcamento.SERVICO_ORIGINAL,
				StatusOrcamento.PENDENTE_APROVACAO, LocalDateTime.now(), LocalDateTime.now(), null);
	}

}
