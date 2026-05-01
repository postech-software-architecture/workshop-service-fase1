package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoClienteId;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoClienteJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoJpaEntity;
import org.mapstruct.*;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper responsavel por converter o agregado de veiculo entre dominio e persistencia.
 * toDomain e manual pois o construtor exige Collection&lt;UUID&gt; extraida de
 * Set&lt;VeiculoClienteJpaEntity&gt;, o que o MapStruct nao gera automaticamente.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VeiculoMapper {

	@Mapping(target = "placa", source = "placa.valor")
	@Mapping(target = "clientesVinculados", ignore = true)
	VeiculoJpaEntity toEntity(Veiculo veiculo);

	default Veiculo toDomain(VeiculoJpaEntity entity) {
		if (entity == null) {
			return null;
		}
		Set<UUID> clientesIds = entity.getClientesVinculados()
			.stream()
			.map(VeiculoClienteJpaEntity::getClienteId)
			.collect(Collectors.toCollection(LinkedHashSet::new));
		return new Veiculo(entity.getId(), entity.getPlaca(), entity.getMarca(), entity.getModelo(), entity.getAno(),
				entity.getCor(), entity.getObservacoes(), clientesIds, Boolean.TRUE.equals(entity.getAtivo()),
				entity.getDataCriacao(), entity.getDataUltimaAtualizacao(), entity.getDataRemocao());
	}

	@AfterMapping
	default void sincronizarClientes(Veiculo veiculo, @MappingTarget VeiculoJpaEntity entity) {
		Set<UUID> desejados = new LinkedHashSet<>(veiculo.getClientesVinculados());
		entity.getClientesVinculados().removeIf(vinculo -> !desejados.contains(vinculo.getClienteId()));

		Set<UUID> atuais = entity.getClientesVinculados()
			.stream()
			.map(VeiculoClienteJpaEntity::getClienteId)
			.collect(Collectors.toSet());

		for (UUID clienteId : desejados) {
			if (atuais.contains(clienteId)) {
				entity.getClientesVinculados()
					.stream()
					.filter(vinculo -> vinculo.getClienteId().equals(clienteId))
					.findFirst()
					.ifPresent(vinculo -> vinculo.setDataUltimaAtualizacao(veiculo.getDataUltimaAtualizacao()));
				continue;
			}

			LocalDateTime agora = veiculo.getDataUltimaAtualizacao();
			VeiculoClienteJpaEntity vinculo = VeiculoClienteJpaEntity.builder()
				.id(new VeiculoClienteId(veiculo.getId(), clienteId))
				.veiculo(entity)
				.clienteId(clienteId)
				.dataCriacao(agora)
				.dataUltimaAtualizacao(agora)
				.build();
			entity.getClientesVinculados().add(vinculo);
		}
	}

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "placa", source = "placa.valor")
	@Mapping(target = "dataCriacao", ignore = true)
	@Mapping(target = "dataUltimaAtualizacao", source = "dataUltimaAtualizacao")
	@Mapping(target = "dataRemocao", source = "dataRemocao")
	@Mapping(target = "clientesVinculados", ignore = true)
	void updateEntityFromDomain(Veiculo domain, @MappingTarget VeiculoJpaEntity entity);

}
