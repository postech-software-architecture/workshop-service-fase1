package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.Veiculo;
import com.postech.workshop_service.domain.valueobjects.Placa;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoClienteId;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoClienteJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.entities.VeiculoJpaEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper responsavel por converter o agregado de veiculo entre dominio e persistencia.
 */
@Component
public class VeiculoMapper {

    /**
     * Converte um agregado de dominio em entidade JPA.
     *
     * @param veiculo agregado de dominio.
     * @return entidade JPA correspondente.
     */
    public VeiculoJpaEntity toEntity(Veiculo veiculo) {
        VeiculoJpaEntity entity = VeiculoJpaEntity.builder()
                .id(veiculo.getId())
                .placa(veiculo.getPlaca().getValor())
                .marca(veiculo.getMarca())
                .modelo(veiculo.getModelo())
                .ano(veiculo.getAno())
                .cor(veiculo.getCor())
                .observacoes(veiculo.getObservacoes())
                .ativo(veiculo.isAtivo())
                .dataRemocao(veiculo.getDataRemocao())
                .dataCriacao(veiculo.getDataCriacao())
                .dataUltimaAtualizacao(veiculo.getDataUltimaAtualizacao())
                .build();
        entity.setClientesVinculados(new LinkedHashSet<>());
        sincronizarClientes(veiculo, entity);
        return entity;
    }

    /**
     * Atualiza uma entidade JPA existente com os dados do agregado de dominio.
     *
     * @param veiculo agregado de dominio.
     * @param entity entidade JPA a ser atualizada.
     */
    public void updateEntityFromDomain(Veiculo veiculo, VeiculoJpaEntity entity) {
        entity.setPlaca(veiculo.getPlaca().getValor());
        entity.setMarca(veiculo.getMarca());
        entity.setModelo(veiculo.getModelo());
        entity.setAno(veiculo.getAno());
        entity.setCor(veiculo.getCor());
        entity.setObservacoes(veiculo.getObservacoes());
        entity.setAtivo(veiculo.isAtivo());
        entity.setDataRemocao(veiculo.getDataRemocao());
        entity.setDataUltimaAtualizacao(veiculo.getDataUltimaAtualizacao());
        sincronizarClientes(veiculo, entity);
    }

    /**
     * Converte uma entidade JPA em agregado de dominio.
     *
     * @param entity entidade JPA persistida.
     * @return agregado de dominio correspondente.
     */
    public Veiculo toDomain(VeiculoJpaEntity entity) {
        Set<UUID> clientesIds = entity.getClientesVinculados().stream()
                .map(VeiculoClienteJpaEntity::getClienteId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new Veiculo(
                entity.getId(),
                new Placa(entity.getPlaca()),
                entity.getMarca(),
                entity.getModelo(),
                entity.getAno(),
                entity.getCor(),
                entity.getObservacoes(),
                clientesIds,
                Boolean.TRUE.equals(entity.getAtivo()),
                entity.getDataCriacao(),
                entity.getDataUltimaAtualizacao(),
                entity.getDataRemocao()
        );
    }

    private void sincronizarClientes(Veiculo veiculo, VeiculoJpaEntity entity) {
        Set<UUID> desejados = new LinkedHashSet<>(veiculo.getClientesVinculados());
        entity.getClientesVinculados().removeIf(vinculo -> !desejados.contains(vinculo.getClienteId()));

        Set<UUID> atuais = entity.getClientesVinculados().stream()
                .map(VeiculoClienteJpaEntity::getClienteId)
                .collect(Collectors.toSet());

        for (UUID clienteId : desejados) {
            if (atuais.contains(clienteId)) {
                entity.getClientesVinculados().stream()
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
}
