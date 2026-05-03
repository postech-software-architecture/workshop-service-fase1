package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.valueobjects.Documento;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import com.postech.workshop_service.infrastructure.persistence.entities.ClienteJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.EstoqueJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.MovimentacaoEstoqueJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.PecaInsumoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.ServicoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.ClienteMapper;
import com.postech.workshop_service.infrastructure.persistence.mappers.EstoqueMapper;
import com.postech.workshop_service.infrastructure.persistence.mappers.MovimentacaoEstoqueMapper;
import com.postech.workshop_service.infrastructure.persistence.mappers.PecaInsumoMapper;
import com.postech.workshop_service.infrastructure.persistence.mappers.ServicoMapper;
import com.postech.workshop_service.infrastructure.persistence.mappers.VeiculoMapper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryCoverageTest {

	private final LocalDateTime agora = LocalDateTime.now();

	@Test
	void deveCobrirClienteRepository() {
		JpaClienteRepository jpa = mock(JpaClienteRepository.class);
		ClienteMapper mapper = mock(ClienteMapper.class);
		ClienteRepositoryImpl repository = new ClienteRepositoryImpl(jpa, mapper);
		UUID id = UUID.randomUUID();
		Cliente cliente = new Cliente(id, "Cliente", new Documento("98765432100"), "e@e.com", null);
		ClienteJpaEntity ativo = ClienteJpaEntity.builder().id(id).ativo(true).build();
		ClienteJpaEntity inativo = ClienteJpaEntity.builder().id(id).ativo(false).build();

		when(mapper.toDomain(any(ClienteJpaEntity.class))).thenReturn(cliente);
		when(jpa.findById(id)).thenReturn(Optional.of(inativo), Optional.of(inativo), Optional.of(ativo));
		when(jpa.findByDocumento("98765432100")).thenReturn(Optional.of(ativo));
		when(jpa.findAll(ArgumentMatchers.<Specification<ClienteJpaEntity>>any(), any(Pageable.class)))
			.thenAnswer(invocation -> {
				executarSpec(invocation.getArgument(0));
				return new PageImpl<>(List.of(ativo));
			});
		when(jpa.count()).thenReturn(1L);

		assertThat(repository.buscarPorId(id, false)).isEmpty();
		assertThat(repository.buscarPorId(id, true)).contains(cliente);
		assertThat(repository.buscarPorDocumento("98765432100", true)).contains(cliente);
		assertThat(repository.listar(0, 10, true)).contains(cliente);
		assertThat(repository.listar(0, 10, false)).contains(cliente);
		assertThat(repository.contarTodos()).isEqualTo(1L);
	}

	@Test
	void deveCobrirVeiculoRepository() {
		JpaVeiculoRepository jpa = mock(JpaVeiculoRepository.class);
		VeiculoMapper mapper = mock(VeiculoMapper.class);
		VeiculoRepositoryImpl repository = new VeiculoRepositoryImpl(jpa, mapper);
		UUID id = UUID.randomUUID();
		UUID clienteId = UUID.randomUUID();
		Veiculo veiculo = new Veiculo("BRA1D23", "Toyota", "Corolla", 2020, null, null, List.of(clienteId));
		VeiculoJpaEntity ativo = VeiculoJpaEntity.builder().id(id).placa("BRA1D23").ativo(true).build();
		VeiculoJpaEntity inativo = VeiculoJpaEntity.builder().id(id).placa("BRA1D23").ativo(false).build();

		when(mapper.toDomain(any(VeiculoJpaEntity.class))).thenReturn(veiculo);
		when(jpa.findByPlaca("BRA1D23")).thenReturn(Optional.of(inativo), Optional.of(inativo));
		when(jpa.findAll(ArgumentMatchers.<Specification<VeiculoJpaEntity>>any(), any(Pageable.class)))
			.thenAnswer(invocation -> {
				executarSpec(invocation.getArgument(0));
				return new PageImpl<>(List.of(ativo));
			});
		when(jpa.findAll(ArgumentMatchers.<Specification<VeiculoJpaEntity>>any())).thenAnswer(invocation -> {
			executarSpec(invocation.getArgument(0));
			return List.of(ativo);
		});

		assertThat(repository.buscarPorPlaca("BRA1D23", false)).isEmpty();
		assertThat(repository.buscarPorPlaca("BRA1D23", true)).contains(veiculo);
		assertThat(repository.listar(0, 10, "BRA1D23", clienteId, false).itens()).contains(veiculo);
		assertThat(repository.listarPorCliente(clienteId, true)).contains(veiculo);
	}

	@Test
	void deveCobrirReposDeEstoquePecaServicoEMovimentacao() {
		cobrirEstoqueRepository();
		cobrirPecaRepository();
		cobrirServicoRepository();
		cobrirMovimentacaoRepository();
	}

	private void cobrirEstoqueRepository() {
		JpaEstoqueRepository jpa = mock(JpaEstoqueRepository.class);
		EstoqueMapper mapper = mock(EstoqueMapper.class);
		EstoqueRepositoryImpl repository = new EstoqueRepositoryImpl(jpa, mapper);
		UUID id = UUID.randomUUID();
		Estoque estoque = new Estoque(id, UUID.randomUUID(), "A1", BigDecimal.ONE, true, 0, agora, agora);
		EstoqueJpaEntity inativo = EstoqueJpaEntity.builder().id(id).ativo(false).build();
		EstoqueJpaEntity ativo = EstoqueJpaEntity.builder().id(id).ativo(true).build();

		when(mapper.toDomain(any(EstoqueJpaEntity.class))).thenReturn(estoque);
		when(jpa.findById(id)).thenReturn(Optional.of(inativo), Optional.of(inativo), Optional.of(ativo));

		assertThat(repository.buscarPorId(id, false)).isEmpty();
		assertThat(repository.buscarPorId(id, true)).contains(estoque);
	}

	private void cobrirPecaRepository() {
		JpaPecaInsumoRepository jpa = mock(JpaPecaInsumoRepository.class);
		PecaInsumoMapper mapper = mock(PecaInsumoMapper.class);
		PecaInsumoRepositoryImpl repository = new PecaInsumoRepositoryImpl(jpa, mapper);
		PecaInsumo peca = mock(PecaInsumo.class);
		PecaInsumoJpaEntity entity = PecaInsumoJpaEntity.builder().ativo(true).build();

		when(mapper.toDomain(any(PecaInsumoJpaEntity.class))).thenReturn(peca);
		when(jpa.findAll(ArgumentMatchers.<Specification<PecaInsumoJpaEntity>>any(), any(Pageable.class)))
			.thenAnswer(invocation -> {
				executarSpec(invocation.getArgument(0));
				return new PageImpl<>(List.of(entity));
			});

		assertThat(repository.listar(0, 10, "Filtro", "Motor", false).itens()).contains(peca);
		assertThat(repository.listar(0, 10, " ", " ", true).itens()).contains(peca);
	}

	private void cobrirServicoRepository() {
		JpaServicoRepository jpa = mock(JpaServicoRepository.class);
		ServicoMapper mapper = mock(ServicoMapper.class);
		ServicoRepositoryImpl repository = new ServicoRepositoryImpl(jpa, mapper);
		Servico servico = mock(Servico.class);
		ServicoJpaEntity entity = ServicoJpaEntity.builder().ativo(true).build();

		when(mapper.toDomain(any(ServicoJpaEntity.class))).thenReturn(servico);
		when(jpa.findAll(ArgumentMatchers.<Specification<ServicoJpaEntity>>any(), any(Pageable.class)))
			.thenAnswer(invocation -> {
				executarSpec(invocation.getArgument(0));
				return new PageImpl<>(List.of(entity));
			});

		assertThat(repository.listar(0, 10, "Troca", CategoriaServico.MECANICA, false).itens()).contains(servico);
		assertThat(repository.listar(0, 10, " ", null, true).itens()).contains(servico);
	}

	private void cobrirMovimentacaoRepository() {
		JpaMovimentacaoEstoqueRepository jpa = mock(JpaMovimentacaoEstoqueRepository.class);
		MovimentacaoEstoqueMapper mapper = mock(MovimentacaoEstoqueMapper.class);
		MovimentacaoEstoqueRepositoryImpl repository = new MovimentacaoEstoqueRepositoryImpl(jpa, mapper);
		UUID estoqueId = UUID.randomUUID();
		MovimentacaoEstoque domain = mock(MovimentacaoEstoque.class);
		MovimentacaoEstoqueJpaEntity antes = movimentacao("ENTRADA", agora.minusDays(2));
		MovimentacaoEstoqueJpaEntity dentro = movimentacao("ENTRADA", agora);
		MovimentacaoEstoqueJpaEntity depois = movimentacao("ENTRADA", agora.plusDays(2));

		when(mapper.toDomain(any(MovimentacaoEstoqueJpaEntity.class))).thenReturn(domain);
		when(jpa.findByEstoqueIdOrderByDataMovimentacaoDesc(estoqueId)).thenReturn(List.of(antes, dentro, depois));

		assertThat(
				repository.listarPorEstoque(estoqueId, TipoMovimentacao.ENTRADA, agora.minusDays(1), agora.plusDays(1)))
			.containsExactly(domain);
		assertThat(repository.listarPorEstoque(estoqueId, null, null, null)).hasSize(3);
	}

	private MovimentacaoEstoqueJpaEntity movimentacao(String tipo, LocalDateTime data) {
		return MovimentacaoEstoqueJpaEntity.builder()
			.tipo(tipo)
			.dataMovimentacao(data)
			.quantidade(BigDecimal.ONE)
			.quantidadeAnterior(BigDecimal.ZERO)
			.quantidadePosterior(BigDecimal.ONE)
			.build();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void executarSpec(Specification spec) {
		Root root = mock(Root.class);
		CriteriaQuery query = mock(CriteriaQuery.class);
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		Path path = mock(Path.class);
		Expression expression = mock(Expression.class);
		Predicate predicate = mock(Predicate.class);
		Join join = mock(Join.class);
		when(cb.conjunction()).thenReturn(predicate);
		when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
		when(cb.isTrue(any(Expression.class))).thenReturn(predicate);
		when(cb.equal(any(Expression.class), any())).thenReturn(predicate);
		when(cb.like(any(Expression.class), anyString())).thenReturn(predicate);
		when(cb.lower(any(Expression.class))).thenReturn(expression);
		when(root.get(anyString())).thenReturn(path);
		when(root.join(anyString(), any())).thenReturn(join);
		when(join.get(anyString())).thenReturn(path);
		spec.toPredicate(root, query, cb);
	}

}
