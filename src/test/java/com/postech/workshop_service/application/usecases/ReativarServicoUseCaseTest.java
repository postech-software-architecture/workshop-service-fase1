package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReativarServicoUseCaseTest {

	@Mock
	private ServicoRepository servicoRepository;

	@InjectMocks
	private ReativarServicoUseCase reativarServicoUseCase;

	@Test
	void shouldReativarServicoInativo() {
		UUID id = UUID.randomUUID();
		Servico servico = criarServico(id, "Troca de oleo", "Descricao", new BigDecimal("100.00"));
		servico.removerLogicamente();

		when(servicoRepository.buscarPorId(id, true)).thenReturn(Optional.of(servico));
		when(servicoRepository.existeNomeAtivo("Troca de oleo", id)).thenReturn(false);
		when(servicoRepository.salvar(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Servico resultado = reativarServicoUseCase.executar(id);

		assertNotNull(resultado);
		assertTrue(resultado.isAtivo());
		verify(servicoRepository).salvar(servico);
	}

	@Test
	void shouldThrowWhenServicoNotFound() {
		UUID id = UUID.randomUUID();
		when(servicoRepository.buscarPorId(id, true)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> reativarServicoUseCase.executar(id));
		verify(servicoRepository, never()).salvar(any(Servico.class));
	}

	@Test
	void shouldRejectWhenAnotherActiveServicoUsesSameName() {
		UUID id = UUID.randomUUID();
		Servico servico = criarServico(id, "Alinhamento", "Descricao", new BigDecimal("80.00"));
		servico.removerLogicamente();

		when(servicoRepository.buscarPorId(id, true)).thenReturn(Optional.of(servico));
		when(servicoRepository.existeNomeAtivo("Alinhamento", id)).thenReturn(true);

		assertThrows(RegraDeNegocioException.class, () -> reativarServicoUseCase.executar(id));
		verify(servicoRepository, never()).salvar(any(Servico.class));
	}

	@Test
	void shouldKeepServicoActiveWhenAlreadyActive() {
		UUID id = UUID.randomUUID();
		Servico servico = criarServico(id, "Lavagem", "Descricao", new BigDecimal("70.00"));

		when(servicoRepository.buscarPorId(id, true)).thenReturn(Optional.of(servico));
		when(servicoRepository.existeNomeAtivo("Lavagem", id)).thenReturn(false);
		when(servicoRepository.salvar(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Servico resultado = reativarServicoUseCase.executar(id);

		assertSame(servico, resultado);
		assertTrue(resultado.isAtivo());
		verify(servicoRepository).salvar(servico);
	}

	private Servico criarServico(UUID id, String nome, String descricao, BigDecimal valor) {
		return new Servico(id, nome, descricao, valor, null, null, null, null);
	}

}
