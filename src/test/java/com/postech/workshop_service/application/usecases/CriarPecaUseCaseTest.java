package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarPecaUseCaseTest {

	@Mock
	private PecaInsumoRepository repository;

	private CriarPecaUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new CriarPecaUseCase(repository);
	}

	@Test
	void deveCriarPecaComSucesso() {
		when(repository.existeSkuAtivo("FIL-001", null)).thenReturn(false);
		when(repository.salvar(any(PecaInsumo.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PecaInsumo resultado = useCase.executar("FIL-001", "Filtro de Oleo", new BigDecimal("45.90"),
				new BigDecimal("5"), "UN", "PECA", null, null, null, null, null, null);

		assertNotNull(resultado);
		assertEquals("FIL-001", resultado.getSku());
		assertEquals("Filtro de Oleo", resultado.getNome());
		assertEquals("PECA", resultado.getTipoItem().name());
		verify(repository).salvar(any(PecaInsumo.class));
	}

	@Test
	void deveLancarExcecaoQuandoSkuJaExiste() {
		when(repository.existeSkuAtivo("FIL-001", null)).thenReturn(true);

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar("FIL-001", "Filtro de Oleo",
				new BigDecimal("45.90"), new BigDecimal("5"), "UN", "PECA", null, null, null, null, null, null));

		verify(repository, never()).salvar(any());
	}

	@Test
	void deveCriarPecaComDadosCompletos() {
		when(repository.existeSkuAtivo("FIL-001", null)).thenReturn(false);
		when(repository.salvar(any(PecaInsumo.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PecaInsumo resultado = useCase.executar("FIL-001", "Filtro de Oleo", new BigDecimal("45.90"),
				new BigDecimal("5"), "UN", "INSUMO", "Bosch", "7891234567890", "Bosch", "Filtros", "Motor 1.0", "Obs");

		assertEquals("INSUMO", resultado.getTipoItem().name());
		assertEquals("Bosch", resultado.getFornecedor());
		assertEquals("7891234567890", resultado.getCodigoBarras());
		assertEquals("Bosch", resultado.getMarca());
		assertEquals("Filtros", resultado.getCategoria());
		assertEquals("Motor 1.0", resultado.getAplicacao());
		assertEquals("Obs", resultado.getObservacoes());
	}

}
