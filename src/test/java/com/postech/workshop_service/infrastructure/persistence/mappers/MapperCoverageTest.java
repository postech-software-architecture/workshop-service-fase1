package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Estoque;
import com.postech.workshop_service.domain.entities.MovimentacaoEstoque;
import com.postech.workshop_service.domain.entities.PecaInsumo;
import com.postech.workshop_service.domain.entities.RefreshToken;
import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.domain.entities.Usuario;
import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.enums.CategoriaServico;
import com.postech.workshop_service.domain.enums.NivelComplexidade;
import com.postech.workshop_service.domain.enums.Role;
import com.postech.workshop_service.domain.valueobjects.Documento;
import com.postech.workshop_service.domain.valueobjects.TipoItem;
import com.postech.workshop_service.domain.valueobjects.TipoMovimentacao;
import com.postech.workshop_service.domain.valueobjects.UnidadeMedida;
import com.postech.workshop_service.infrastructure.persistence.entities.ClienteJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.EnderecoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.EstoqueJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.MovimentacaoEstoqueJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.PecaInsumoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.RefreshTokenJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.ServicoJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.UsuarioJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoClienteId;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoClienteJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoJpaEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MapperCoverageTest {

	private final LocalDateTime agora = LocalDateTime.now();

	@Test
	void deveCobrirMapperDeCliente() {
		ClienteMapperImpl mapper = new ClienteMapperImpl();
		ReflectionTestUtils.setField(mapper, "enderecoMapper", new EnderecoMapperImpl());
		Cliente cliente = new Cliente(UUID.randomUUID(), "Cliente", new Documento("98765432100"), "e@e.com", null);
		ClienteJpaEntity entity = ClienteJpaEntity.builder()
			.id(cliente.getId())
			.nome("Cliente")
			.documento("98765432100")
			.email("e@e.com")
			.ativo(true)
			.dataCriacao(agora)
			.dataUltimaAtualizacao(agora)
			.build();

		assertThat(mapper.toEntity(null)).isNull();
		assertThat(mapper.toDomain(null)).isNull();
		assertThat(mapper.map(null)).isNull();
		assertThat(mapper.toEntity(cliente).getDocumento()).isEqualTo("98765432100");
		assertThat(mapper.toDomain(entity).getDocumento().getValor()).isEqualTo("98765432100");
		mapper.updateEntityFromDomain(null, entity);
		mapper.updateEntityFromDomain(cliente, entity);
		ClienteJpaEntity entityComEnderecoPersistido = ClienteJpaEntity.builder()
			.id(cliente.getId())
			.dataCriacao(agora)
			.dataUltimaAtualizacao(agora)
			.endereco(EnderecoJpaEntity.builder().dataCriacao(agora).dataUltimaAtualizacao(agora).build())
			.build();
		mapper.linkAddress(entityComEnderecoPersistido);

		Cliente clienteSemDocumento = mock(Cliente.class);
		when(clienteSemDocumento.getDocumento()).thenReturn(null);
		assertThat(mapper.toEntity(clienteSemDocumento).getDocumento()).isNull();
	}

	@Test
	void deveCobrirMappersDePecaEstoqueMovimentacaoEServico() {
		PecaInsumoMapperImpl pecaMapper = new PecaInsumoMapperImpl();
		EstoqueMapperImpl estoqueMapper = new EstoqueMapperImpl();
		MovimentacaoEstoqueMapperImpl movimentacaoMapper = new MovimentacaoEstoqueMapperImpl();
		ServicoMapper servicoMapper = new ServicoMapper();
		PecaInsumo peca = new PecaInsumo(UUID.randomUUID(), "SKU-1", "Peca", BigDecimal.TEN, BigDecimal.ONE,
				UnidadeMedida.UN, TipoItem.PECA);
		PecaInsumoJpaEntity pecaEntity = PecaInsumoJpaEntity.builder()
			.id(peca.getId())
			.sku("SKU-1")
			.nome("Peca")
			.valorUnitario(BigDecimal.TEN)
			.estoqueMinimo(BigDecimal.ONE)
			.unidadeMedida("UN")
			.tipoItem("PECA")
			.ativo(true)
			.versao(1)
			.dataCriacao(agora)
			.dataUltimaAtualizacao(agora)
			.build();
		Estoque estoque = new Estoque(UUID.randomUUID(), peca.getId(), "A1", BigDecimal.ONE, true, 0, agora, agora);
		EstoqueJpaEntity estoqueEntity = EstoqueJpaEntity.builder()
			.id(estoque.getId())
			.pecaInsumoId(peca.getId())
			.localizacao("A1")
			.quantidade(BigDecimal.ONE)
			.ativo(true)
			.versao(0)
			.dataCriacao(agora)
			.dataUltimaAtualizacao(agora)
			.build();
		MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(UUID.randomUUID(), estoque.getId(),
				TipoMovimentacao.ENTRADA, BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE, "Entrada", agora, agora);
		MovimentacaoEstoqueJpaEntity movimentacaoEntity = MovimentacaoEstoqueJpaEntity.builder()
			.id(movimentacao.getId())
			.estoqueId(estoque.getId())
			.tipo("ENTRADA")
			.quantidade(BigDecimal.ONE)
			.quantidadeAnterior(BigDecimal.ZERO)
			.quantidadePosterior(BigDecimal.ONE)
			.dataMovimentacao(agora)
			.dataCriacao(agora)
			.build();
		Servico servico = new Servico(UUID.randomUUID(), "Servico", "Descricao", BigDecimal.TEN,
				CategoriaServico.MECANICA, NivelComplexidade.BAIXA, 30, "Obs", true, agora, agora, null);
		ServicoJpaEntity servicoEntity = ServicoJpaEntity.builder()
			.id(servico.getId())
			.nome("Servico")
			.descricao("Descricao")
			.valor(BigDecimal.TEN)
			.ativo(true)
			.dataCriacao(agora)
			.dataUltimaAtualizacao(agora)
			.build();

		assertThat(pecaMapper.toEntity(null)).isNull();
		assertThat(pecaMapper.toDomain(null)).isNull();
		assertThat(pecaMapper.mapUnidadeMedida(null)).isNull();
		assertThat(pecaMapper.mapTipoItem(null)).isNull();
		assertThat(pecaMapper.toEntity(peca).getUnidadeMedida()).isEqualTo("UN");
		assertThat(pecaMapper.toDomain(pecaEntity).getTipoItem()).isEqualTo(TipoItem.PECA);
		pecaMapper.updateEntityFromDomain(null, pecaEntity);
		pecaMapper.updateEntityFromDomain(peca, pecaEntity);

		assertThat(estoqueMapper.toEntity(null)).isNull();
		assertThat(estoqueMapper.toDomain(null)).isNull();
		assertThat(estoqueMapper.toEntity(estoque).getLocalizacao()).isEqualTo("A1");
		assertThat(estoqueMapper.toDomain(estoqueEntity).getQuantidade()).isEqualTo(BigDecimal.ONE);
		estoqueMapper.updateEntityFromDomain(null, estoqueEntity);
		estoqueMapper.updateEntityFromDomain(estoque, estoqueEntity);

		assertThat(movimentacaoMapper.toEntity(null)).isNull();
		assertThat(movimentacaoMapper.toDomain(null)).isNull();
		assertThat(movimentacaoMapper.mapTipoMovimentacao(null)).isNull();
		assertThat(movimentacaoMapper.toEntity(movimentacao).getTipo()).isEqualTo("ENTRADA");
		assertThat(movimentacaoMapper.toDomain(movimentacaoEntity).getTipo()).isEqualTo(TipoMovimentacao.ENTRADA);

		assertThat(servicoMapper.toEntity(null)).isNull();
		assertThat(servicoMapper.toDomain(null)).isNull();
		assertThat(servicoMapper.toEntity(servico).getNome()).isEqualTo("Servico");
		assertThat(servicoMapper.toDomain(servicoEntity).getNome()).isEqualTo("Servico");
		servicoMapper.updateEntityFromDomain(null, servicoEntity);
		servicoMapper.updateEntityFromDomain(servico, null);
		servicoMapper.updateEntityFromDomain(servico, servicoEntity);
	}

	@Test
	void deveCobrirMapperDeVeiculo() {
		VeiculoMapperImpl mapper = new VeiculoMapperImpl();
		UUID clienteA = UUID.randomUUID();
		UUID clienteB = UUID.randomUUID();
		Veiculo veiculo = new Veiculo(UUID.randomUUID(), "BRA1D23", "Toyota", "Corolla", 2020, null, null,
				List.of(clienteA, clienteB), true, agora, agora, null);
		VeiculoJpaEntity entity = VeiculoJpaEntity.builder()
			.id(veiculo.getId())
			.placa("BRA1D23")
			.marca("Toyota")
			.modelo("Corolla")
			.ano(2020)
			.ativo(true)
			.dataCriacao(agora)
			.dataUltimaAtualizacao(agora)
			.clientesVinculados(new LinkedHashSet<>())
			.build();
		entity.getClientesVinculados()
			.add(VeiculoClienteJpaEntity.builder()
				.id(new VeiculoClienteId(veiculo.getId(), clienteA))
				.veiculo(entity)
				.clienteId(clienteA)
				.dataCriacao(agora.minusDays(1))
				.dataUltimaAtualizacao(agora.minusDays(1))
				.build());
		entity.getClientesVinculados()
			.add(VeiculoClienteJpaEntity.builder()
				.id(new VeiculoClienteId(veiculo.getId(), UUID.randomUUID()))
				.veiculo(entity)
				.clienteId(UUID.randomUUID())
				.dataCriacao(agora.minusDays(1))
				.dataUltimaAtualizacao(agora.minusDays(1))
				.build());

		assertThat(mapper.toEntity(null)).isNull();
		assertThat(mapper.toDomain(null)).isNull();
		assertThat(mapper.toEntity(veiculo).getPlaca()).isEqualTo("BRA1D23");
		assertThat(mapper.toDomain(entity).getClientesVinculados()).contains(clienteA);
		mapper.updateEntityFromDomain(null, entity);
		mapper.updateEntityFromDomain(veiculo, entity);
		assertThat(entity.getClientesVinculados()).extracting(VeiculoClienteJpaEntity::getClienteId)
			.containsExactlyInAnyOrder(clienteA, clienteB);

		Veiculo veiculoSemPlaca = mock(Veiculo.class);
		when(veiculoSemPlaca.getPlaca()).thenReturn(null);
		when(veiculoSemPlaca.getClientesVinculados()).thenReturn(Set.of(clienteA));
		assertThat(mapper.toEntity(veiculoSemPlaca).getPlaca()).isNull();
	}

	@Test
	void deveCobrirMappersDeUsuarioERefreshToken() {
		UsuarioMapperImpl usuarioMapper = new UsuarioMapperImpl();
		RefreshTokenMapperImpl refreshMapper = new RefreshTokenMapperImpl();
		UUID usuarioId = UUID.randomUUID();
		Usuario usuario = new Usuario(usuarioId, "admin", "admin@teste.com", "hash", Set.of(Role.ADMINISTRADOR), null,
				true, false, agora, agora, null);
		UsuarioJpaEntity usuarioEntity = UsuarioJpaEntity.builder()
			.id(usuarioId)
			.username("admin")
			.email("admin@teste.com")
			.senhaHash("hash")
			.roles(new LinkedHashSet<>(Set.of(Role.ADMINISTRADOR)))
			.ativo(true)
			.bloqueado(false)
			.dataCriacao(agora)
			.dataUltimaAtualizacao(agora)
			.build();
		RefreshToken refreshToken = new RefreshToken(UUID.randomUUID(), "token", usuarioId, agora.plusDays(1), false,
				null, agora, agora, null);
		RefreshTokenJpaEntity refreshEntity = RefreshTokenJpaEntity.builder()
			.id(refreshToken.getId())
			.token("token")
			.usuario(usuarioEntity)
			.dataExpiracao(agora.plusDays(1))
			.revogado(false)
			.dataCriacao(agora)
			.dataUltimaAtualizacao(agora)
			.build();

		assertThat(usuarioMapper.toEntity(null)).isNull();
		assertThat(usuarioMapper.toDomain(null)).isNull();
		assertThat(usuarioMapper.map(null)).isNull();
		assertThat(usuarioMapper.toEntity(usuario).getUsername()).isEqualTo("admin");
		assertThat(usuarioMapper.toDomain(usuarioEntity).getUsername()).isEqualTo("admin");
		usuarioMapper.updateEntityFromDomain(null, usuarioEntity);
		usuarioMapper.updateEntityFromDomain(usuario, usuarioEntity);

		Usuario usuarioSemRoles = mock(Usuario.class);
		when(usuarioSemRoles.getRoles()).thenReturn(null);
		assertThat(usuarioMapper.toEntity(usuarioSemRoles).getRoles()).isEmpty();
		usuarioMapper.updateEntityFromDomain(usuarioSemRoles, usuarioEntity);
		assertThat(usuarioEntity.getRoles()).isNull();
		usuarioEntity.setRoles(null);
		assertThatThrownBy(() -> usuarioMapper.toDomain(usuarioEntity)).isInstanceOf(RuntimeException.class);
		usuarioMapper.updateEntityFromDomain(usuario, usuarioEntity);
		assertThat(usuarioEntity.getRoles()).contains(Role.ADMINISTRADOR);
		usuarioEntity.setRoles(null);
		usuarioMapper.updateEntityFromDomain(usuarioSemRoles, usuarioEntity);

		assertThat(refreshMapper.toEntity(null)).isNull();
		assertThat(refreshMapper.toDomain(null)).isNull();
		assertThat(refreshMapper.map(null)).isNull();
		assertThat(refreshMapper.toEntity(refreshToken).getToken()).isEqualTo("token");
		assertThat(refreshMapper.toDomain(refreshEntity).getUsuarioId()).isEqualTo(usuarioId);
		refreshMapper.updateEntityFromDomain(null, refreshEntity);
		refreshMapper.updateEntityFromDomain(refreshToken, refreshEntity);
	}

}
