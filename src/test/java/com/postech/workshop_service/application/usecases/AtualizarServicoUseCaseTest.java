package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import com.postech.workshop_service.domain.repositories.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarServicoUseCaseTest {

	@Mock
	private ServicoRepository servicoRepository;

	@InjectMocks
	private AtualizarServicoUseCase atualizarServicoUseCase;

	@Test
	void shouldUpdateServico() {
		UUID id = UUID.randomUUID();
		Servico servico = criarServico(id, "Nome original", "Descricao original", new BigDecimal("100.00"));

		when(servicoRepository.buscarPorId(id, true)).thenReturn(Optional.of(servico));
		when(servicoRepository.existeNomeAtivo("Nome atualizado", id)).thenReturn(false);
		when(servicoRepository.salvar(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Servico atualizado = atualizarServicoUseCase.executar(id, "Nome atualizado", "Nova descricao",
				new BigDecimal("200.00"), CategoriaServico.MECANICA, NivelComplexidade.ALTA, 60, "Nova obs");

		assertEquals("Nome atualizado", atualizado.getNome());
		assertEquals(new BigDecimal("200.00"), atualizado.getValor());
		assertEquals(CategoriaServico.MECANICA, atualizado.getCategoria());
	}

	@Test
	void shouldThrowWhenServicoNotFound() {
		UUID id = UUID.randomUUID();
		when(servicoRepository.buscarPorId(id, true)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> atualizarServicoUseCase.executar(id, "Nome",
				"Descricao", new BigDecimal("100.00"), null, null, null, null));
	}

	@Test
	void shouldRejectDuplicateNameOnUpdate() {
		UUID id = UUID.randomUUID();
		Servico servico = criarServico(id, "Nome original", "Descricao", new BigDecimal("100.00"));

		when(servicoRepository.buscarPorId(id, true)).thenReturn(Optional.of(servico));
		when(servicoRepository.existeNomeAtivo("Nome duplicado", id)).thenReturn(true);

		assertThrows(RegraDeNegocioException.class, () -> atualizarServicoUseCase.executar(id, "Nome duplicado",
				"Descricao", new BigDecimal("100.00"), null, null, null, null));
	}

	private Servico criarServico(UUID id, String nome, String descricao, BigDecimal valor) {
		return new Servico(id, nome, descricao, valor, null, null, null, null);
	}

}
