package com.postech.workshop_service.application.usecases;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CriarServicoUseCaseTest {

	@Mock
	private ServicoRepository servicoRepository;

	@InjectMocks
	private CriarServicoUseCase criarServicoUseCase;

	@Test
	void shouldCreateServico() {
		when(servicoRepository.existeNomeAtivo("Troca de oleo", null)).thenReturn(false);
		when(servicoRepository.salvar(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Servico servico = criarServicoUseCase.executar("Troca de oleo", "Descricao do servico",
				new BigDecimal("100.00"), CategoriaServico.PREVENTIVA, NivelComplexidade.BAIXA, 30, null);

		assertEquals("Troca de oleo", servico.getNome());
		assertEquals(new BigDecimal("100.00"), servico.getValor());
	}

	@Test
	void shouldRejectDuplicateName() {
		when(servicoRepository.existeNomeAtivo("Troca de oleo", null)).thenReturn(true);

		assertThrows(RegraDeNegocioException.class, () -> criarServicoUseCase.executar("Troca de oleo", "Descricao",
				new BigDecimal("100.00"), CategoriaServico.PREVENTIVA, NivelComplexidade.BAIXA, null, null));
	}

	@Test
	void shouldRejectInvalidValue() {
		when(servicoRepository.existeNomeAtivo("Servico X", null)).thenReturn(false);

		assertThrows(RegraDeNegocioException.class, () -> criarServicoUseCase.executar("Servico X", "Descricao",
				new BigDecimal("-1.00"), null, null, null, null));
	}

}
