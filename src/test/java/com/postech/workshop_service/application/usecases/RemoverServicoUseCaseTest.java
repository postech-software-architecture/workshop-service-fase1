package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoverServicoUseCaseTest {

	@Mock
	private ServicoRepository servicoRepository;

	@InjectMocks
	private RemoverServicoUseCase removerServicoUseCase;

	@Test
	void shouldRemoveServico() {
		UUID id = UUID.randomUUID();
		Servico servico = criarServico(id, "Troca de oleo", "Descricao", new BigDecimal("100.00"), 60);
		when(servicoRepository.buscarPorId(id)).thenReturn(Optional.of(servico));
		when(servicoRepository.salvar(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

		removerServicoUseCase.executar(id);

		assertFalse(servico.isAtivo());
	}

	@Test
	void shouldThrowWhenServicoNotFound() {
		assertThrows(RecursoNaoEncontradoException.class, () -> removerServicoUseCase.executar(UUID.randomUUID()));
	}

	private Servico criarServico(UUID id, String nome, String descricao, BigDecimal valor, int tempoEstimadoMinutos) {
		return new Servico(id, nome, descricao, valor, tempoEstimadoMinutos, null, null, null, null);
	}

}
