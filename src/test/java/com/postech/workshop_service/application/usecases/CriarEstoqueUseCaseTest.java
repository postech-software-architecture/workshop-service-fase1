package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.PecaInsumoRepository;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarEstoqueUseCaseTest {

	@Mock
	private EstoqueRepository estoqueRepository;

	@Mock
	private PecaInsumoRepository pecaRepository;

	private CriarEstoqueUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new CriarEstoqueUseCase(estoqueRepository, pecaRepository);
	}

	@Test
	void deveCriarEstoqueComSucesso() {
		UUID pecaId = UUID.randomUUID();
		PecaInsumo peca = new PecaInsumo(pecaId, "FIL-001", "Filtro de Oleo", new BigDecimal("45.90"),
				new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA);

		when(pecaRepository.buscarPorId(pecaId, false)).thenReturn(Optional.of(peca));
		when(estoqueRepository.existeLocalizacao(pecaId, "Prateleira A1", null)).thenReturn(false);
		when(estoqueRepository.salvar(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Estoque resultado = useCase.executar(pecaId, "Prateleira A1", new BigDecimal("10"));

		assertNotNull(resultado);
		assertEquals(pecaId, resultado.getPecaInsumoId());
		assertEquals("Prateleira A1", resultado.getLocalizacao());
		assertEquals(new BigDecimal("10"), resultado.getQuantidade());
		verify(estoqueRepository).salvar(any(Estoque.class));
	}

	@Test
	void deveLancarExcecaoQuandoPecaNaoExiste() {
		UUID pecaId = UUID.randomUUID();
		when(pecaRepository.buscarPorId(pecaId, false)).thenReturn(Optional.empty());

		assertThrows(RegraDeNegocioException.class,
				() -> useCase.executar(pecaId, "Prateleira A1", new BigDecimal("10")));

		verify(estoqueRepository, never()).salvar(any());
	}

	@Test
	void deveLancarExcecaoQuandoLocalizacaoJaExiste() {
		UUID pecaId = UUID.randomUUID();
		PecaInsumo peca = new PecaInsumo(pecaId, "FIL-001", "Filtro de Oleo", new BigDecimal("45.90"),
				new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA);

		when(pecaRepository.buscarPorId(pecaId, false)).thenReturn(Optional.of(peca));
		when(estoqueRepository.existeLocalizacao(pecaId, "Prateleira A1", null)).thenReturn(true);

		assertThrows(RegraDeNegocioException.class,
				() -> useCase.executar(pecaId, "Prateleira A1", new BigDecimal("10")));

		verify(estoqueRepository, never()).salvar(any());
	}

	@Test
	void deveCriarEstoqueComQuantidadeZero() {
		UUID pecaId = UUID.randomUUID();
		PecaInsumo peca = new PecaInsumo(pecaId, "FIL-001", "Filtro de Oleo", new BigDecimal("45.90"),
				new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA);

		when(pecaRepository.buscarPorId(pecaId, false)).thenReturn(Optional.of(peca));
		when(estoqueRepository.existeLocalizacao(pecaId, "Prateleira A1", null)).thenReturn(false);
		when(estoqueRepository.salvar(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Estoque resultado = useCase.executar(pecaId, "Prateleira A1", BigDecimal.ZERO);

		assertEquals(BigDecimal.ZERO, resultado.getQuantidade());
	}

	@Test
	void deveNormalizarLocalizacaoEConverterErroDeValidacao() {
		UUID pecaId = UUID.randomUUID();
		PecaInsumo peca = new PecaInsumo(pecaId, "FIL-001", "Filtro de Oleo", new BigDecimal("45.90"),
				new BigDecimal("5"), UnidadeMedida.UN, TipoItem.PECA);
		when(pecaRepository.buscarPorId(pecaId, false)).thenReturn(Optional.of(peca));
		when(estoqueRepository.existeLocalizacao(pecaId, "Prateleira A1", null)).thenReturn(false);
		when(estoqueRepository.salvar(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Estoque resultado = useCase.executar(pecaId, "  Prateleira A1  ", BigDecimal.ONE);

		assertEquals("Prateleira A1", resultado.getLocalizacao());
		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(pecaId, null, BigDecimal.ONE));
		assertThrows(RegraDeNegocioException.class, () -> useCase.executar(pecaId, " ", BigDecimal.ONE));
	}

}
