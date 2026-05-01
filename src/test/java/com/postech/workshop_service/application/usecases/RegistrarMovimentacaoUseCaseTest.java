package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RegraDeNegocioException;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.repositories.EstoqueRepository;
import com.postech.workshop_service.domain.repositories.MovimentacaoEstoqueRepository;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarMovimentacaoUseCaseTest {

	@Mock
	private EstoqueRepository estoqueRepository;

	@Mock
	private MovimentacaoEstoqueRepository movimentacaoRepository;

	private RegistrarMovimentacaoUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new RegistrarMovimentacaoUseCase(estoqueRepository, movimentacaoRepository);
	}

	@Test
	void deveRegistrarEntradaComSucesso() {
		UUID estoqueId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		Estoque estoque = new Estoque(estoqueId, UUID.randomUUID(), "Prateleira A1", new BigDecimal("10"), true, 0,
				agora, agora);

		when(estoqueRepository.buscarPorId(estoqueId, false)).thenReturn(Optional.of(estoque));
		when(movimentacaoRepository.salvar(any(MovimentacaoEstoque.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		when(estoqueRepository.salvar(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

		MovimentacaoEstoque resultado = useCase.executar(estoqueId, "ENTRADA", new BigDecimal("5"), "Reposicao");

		assertNotNull(resultado);
		assertEquals(TipoMovimentacao.ENTRADA, resultado.getTipo());
		assertEquals(new BigDecimal("5"), resultado.getQuantidade());
		assertEquals(new BigDecimal("10"), resultado.getQuantidadeAnterior());
		assertEquals(new BigDecimal("15"), resultado.getQuantidadePosterior());
		verify(movimentacaoRepository).salvar(any(MovimentacaoEstoque.class));
		verify(estoqueRepository).salvar(any(Estoque.class));
	}

	@Test
	void deveRegistrarSaidaComSucesso() {
		UUID estoqueId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		Estoque estoque = new Estoque(estoqueId, UUID.randomUUID(), "Prateleira A1", new BigDecimal("10"), true, 0,
				agora, agora);

		when(estoqueRepository.buscarPorId(estoqueId, false)).thenReturn(Optional.of(estoque));
		when(movimentacaoRepository.salvar(any(MovimentacaoEstoque.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		when(estoqueRepository.salvar(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

		MovimentacaoEstoque resultado = useCase.executar(estoqueId, "SAIDA", new BigDecimal("3"), "Venda");

		assertEquals(TipoMovimentacao.SAIDA, resultado.getTipo());
		assertEquals(new BigDecimal("7"), resultado.getQuantidadePosterior());
	}

	@Test
	void deveRegistrarAjusteComSucesso() {
		UUID estoqueId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		Estoque estoque = new Estoque(estoqueId, UUID.randomUUID(), "Prateleira A1", new BigDecimal("10"), true, 0,
				agora, agora);

		when(estoqueRepository.buscarPorId(estoqueId, false)).thenReturn(Optional.of(estoque));
		when(movimentacaoRepository.salvar(any(MovimentacaoEstoque.class)))
			.thenAnswer(invocation -> invocation.getArgument(0));
		when(estoqueRepository.salvar(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));

		MovimentacaoEstoque resultado = useCase.executar(estoqueId, "AJUSTE", new BigDecimal("20"), "Contagem fisica");

		assertEquals(TipoMovimentacao.AJUSTE, resultado.getTipo());
		assertEquals(new BigDecimal("20"), resultado.getQuantidadePosterior());
	}

	@Test
	void deveLancarExcecaoQuandoEstoqueNaoExiste() {
		UUID estoqueId = UUID.randomUUID();
		when(estoqueRepository.buscarPorId(estoqueId, false)).thenReturn(Optional.empty());

		assertThrows(RegraDeNegocioException.class,
				() -> useCase.executar(estoqueId, "ENTRADA", new BigDecimal("5"), "Reposicao"));

		verify(movimentacaoRepository, never()).salvar(any());
	}

	@Test
	void deveLancarExcecaoQuandoTipoInvalido() {
		UUID estoqueId = UUID.randomUUID();

		assertThrows(RegraDeNegocioException.class,
				() -> useCase.executar(estoqueId, "INVALIDO", new BigDecimal("5"), "Teste"));
	}

	@Test
	void deveLancarExcecaoQuandoSaidaMaiorQueEstoque() {
		UUID estoqueId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		Estoque estoque = new Estoque(estoqueId, UUID.randomUUID(), "Prateleira A1", new BigDecimal("10"), true, 0,
				agora, agora);
		when(estoqueRepository.buscarPorId(estoqueId, false)).thenReturn(Optional.of(estoque));

		assertThrows(RegraDeNegocioException.class,
				() -> useCase.executar(estoqueId, "SAIDA", new BigDecimal("20"), "Venda"));
	}

	@Test
	void deveLancarExcecaoQuandoAjusteSemMotivo() {
		UUID estoqueId = UUID.randomUUID();
		LocalDateTime agora = LocalDateTime.now();
		Estoque estoque = new Estoque(estoqueId, UUID.randomUUID(), "Prateleira A1", new BigDecimal("10"), true, 0,
				agora, agora);
		when(estoqueRepository.buscarPorId(estoqueId, false)).thenReturn(Optional.of(estoque));

		assertThrows(RegraDeNegocioException.class,
				() -> useCase.executar(estoqueId, "AJUSTE", new BigDecimal("20"), null));
	}

}
