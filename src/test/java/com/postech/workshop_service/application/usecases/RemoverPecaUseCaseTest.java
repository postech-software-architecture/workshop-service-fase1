package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RemoverPecaUseCaseTest {

	@Test
	void deveRemoverPecaLogicamente() {
		PecaInsumoRepository repository = mock(PecaInsumoRepository.class);
		RemoverPecaUseCase useCase = new RemoverPecaUseCase(repository);
		UUID id = UUID.randomUUID();
		PecaInsumo peca = new PecaInsumo(id, "FIL-001", "Filtro", BigDecimal.TEN, BigDecimal.ONE, UnidadeMedida.UN,
				TipoItem.PECA);
		when(repository.buscarPorId(id, false)).thenReturn(Optional.of(peca));

		useCase.executar(id);

		assertFalse(peca.isAtivo());
		verify(repository).salvar(peca);
	}

	@Test
	void deveLancarQuandoPecaNaoExiste() {
		PecaInsumoRepository repository = mock(PecaInsumoRepository.class);
		RemoverPecaUseCase useCase = new RemoverPecaUseCase(repository);
		UUID id = UUID.randomUUID();
		when(repository.buscarPorId(id, false)).thenReturn(Optional.empty());

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(id));

		verify(repository, never()).salvar(org.mockito.ArgumentMatchers.any());
	}

}
