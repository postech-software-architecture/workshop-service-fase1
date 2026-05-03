package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BuscarPecaPorSkuUseCaseTest {

	@Test
	void deveNormalizarSkuAntesDaBusca() {
		PecaInsumoRepository repository = mock(PecaInsumoRepository.class);
		BuscarPecaPorSkuUseCase useCase = new BuscarPecaPorSkuUseCase(repository);
		PecaInsumo peca = mock(PecaInsumo.class);
		when(repository.buscarPorSku("FIL-001", false)).thenReturn(Optional.of(peca));

		Optional<PecaInsumo> resultado = useCase.executar(" fil-001 ", false);

		assertSame(peca, resultado.orElseThrow());
		verify(repository).buscarPorSku("FIL-001", false);
	}

	@Test
	void devePermitirSkuNuloParaRepositorioDecidir() {
		PecaInsumoRepository repository = mock(PecaInsumoRepository.class);
		BuscarPecaPorSkuUseCase useCase = new BuscarPecaPorSkuUseCase(repository);
		when(repository.buscarPorSku(null, true)).thenReturn(Optional.empty());

		useCase.executar(null, true);

		verify(repository).buscarPorSku(null, true);
	}

}
