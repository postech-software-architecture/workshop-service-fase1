package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarPecaUseCaseTest {

	@Mock
	private PecaInsumoRepository repository;

	private AtualizarPecaUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new AtualizarPecaUseCase(repository);
	}

	@Test
	void deveAtualizarPecaComSucesso() {
		UUID id = UUID.randomUUID();
		PecaInsumo peca = criarPeca(id);
		when(repository.buscarPorId(id, false)).thenReturn(Optional.of(peca));
		when(repository.salvar(peca)).thenReturn(peca);

		PecaInsumo resultado = useCase.executar(id, "Filtro Premium", new BigDecimal("60.00"), new BigDecimal("8"),
				"cx", "insumo", "Bosch", "789", "Bosch", "Filtros", "Motor", "Obs");

		assertEquals("Filtro Premium", resultado.getNome());
		assertEquals(UnidadeMedida.CX, resultado.getUnidadeMedida());
		assertEquals(TipoItem.INSUMO, resultado.getTipoItem());
	}

	@Test
	void deveLancarQuandoPecaNaoExiste() {
		UUID id = UUID.randomUUID();
		when(repository.buscarPorId(id, false)).thenReturn(Optional.empty());

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(id, "Filtro", BigDecimal.TEN, BigDecimal.ONE,
				"UN", "PECA", null, null, null, null, null, null));

		verify(repository, never()).salvar(any());
	}

	@Test
	void deveConverterErrosDeValidacao() {
		UUID id = UUID.randomUUID();
		when(repository.buscarPorId(id, false)).thenReturn(Optional.of(criarPeca(id)));

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(id, "Filtro", BigDecimal.TEN, BigDecimal.ONE,
				null, "PECA", null, null, null, null, null, null));
		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(id, "Filtro", BigDecimal.TEN, BigDecimal.ONE,
				" ", "PECA", null, null, null, null, null, null));
		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(id, "Filtro", BigDecimal.TEN, BigDecimal.ONE,
				"x", "PECA", null, null, null, null, null, null));
		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(id, "Filtro", BigDecimal.TEN, BigDecimal.ONE,
				"UN", null, null, null, null, null, null, null));
		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(id, "Filtro", BigDecimal.TEN, BigDecimal.ONE,
				"UN", " ", null, null, null, null, null, null));
		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(id, "Filtro", BigDecimal.TEN, BigDecimal.ONE,
				"UN", "x", null, null, null, null, null, null));
	}

	@Test
	void deveConverterConcorrenciaOtimista() {
		UUID id = UUID.randomUUID();
		PecaInsumo peca = criarPeca(id);
		when(repository.buscarPorId(id, false)).thenReturn(Optional.of(peca));
		when(repository.salvar(peca)).thenThrow(new ObjectOptimisticLockingFailureException(PecaInsumo.class, id));

		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(id, "Filtro", BigDecimal.TEN, BigDecimal.ONE,
				"UN", "PECA", null, null, null, null, null, null));
	}

	private PecaInsumo criarPeca(UUID id) {
		return new PecaInsumo(id, "FIL-001", "Filtro", BigDecimal.TEN, BigDecimal.ONE, UnidadeMedida.UN, TipoItem.PECA);
	}

}
