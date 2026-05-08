package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarMovimentacoesEstoquePorOrdemServicoUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

	@InjectMocks
	private ListarMovimentacoesEstoquePorOrdemServicoUseCase useCase;

	@Test
	void deveListarMovimentacoesPorOrdemServico() {
		UUID ordemServicoId = UUID.randomUUID();
		MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(UUID.randomUUID(), UUID.randomUUID(),
				TipoMovimentacao.RESERVA, BigDecimal.ONE, new BigDecimal("2"), new BigDecimal("1"), "Reserva para OS",
				ordemServicoId, UUID.randomUUID());
		when(ordemServicoRepository.buscarPorId(ordemServicoId))
			.thenReturn(Optional.of(mockOrdemServico(ordemServicoId)));
		when(movimentacaoEstoqueRepository.listarPorOrdemServico(ordemServicoId)).thenReturn(List.of(movimentacao));

		List<MovimentacaoEstoque> resultado = useCase.executar(ordemServicoId);

		assertEquals(1, resultado.size());
		assertEquals(movimentacao.getId(), resultado.get(0).getId());
		verify(movimentacaoEstoqueRepository).listarPorOrdemServico(ordemServicoId);
	}

	@Test
	void deveLancarExcecaoQuandoOrdemNaoExistir() {
		UUID ordemServicoId = UUID.randomUUID();
		when(ordemServicoRepository.buscarPorId(ordemServicoId)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(ordemServicoId));
	}

	private com.postech.workshop_service.domain.entities.OrdemServico mockOrdemServico(UUID id) {
		return new com.postech.workshop_service.domain.entities.OrdemServico(id, UUID.randomUUID(), UUID.randomUUID(),
				com.postech.workshop_service.domain.entities.StatusOrdemServico.AGUARDANDO_RESPOSTA_CLIENTE, List.of(),
				"OS-TESTE", null, java.time.LocalDateTime.now().minusDays(2), java.time.LocalDateTime.now(), null);
	}

}
