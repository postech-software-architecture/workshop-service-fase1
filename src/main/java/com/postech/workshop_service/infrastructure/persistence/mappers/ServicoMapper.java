package com.postech.workshop_service.infrastructure.persistence.mappers;

import com.postech.workshop_service.domain.entities.Servico;
import com.postech.workshop_service.infrastructure.persistence.entities.ServicoJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper responsavel por converter o agregado de servico entre dominio e persistencia. A
 * conversao e feita manualmente para garantir controle total sobre o mapeamento dos
 * campos e evitar dependencia de geracao de codigo em tempo de compilacao.
 */
@Component
public class ServicoMapper {

	/**
	 * Converte o agregado de dominio em entidade JPA para insercao.
	 * @param servico agregado de dominio a ser convertido.
	 * @return entidade JPA pronta para persistencia.
	 */
	public ServicoJpaEntity toEntity(Servico servico) {
		if (servico == null) {
			return null;
		}
		return ServicoJpaEntity.builder()
			.id(servico.getId())
			.nome(servico.getNome())
			.descricao(servico.getDescricao())
			.valor(servico.getValor())
			.categoria(servico.getCategoria())
			.nivelComplexidade(servico.getNivelComplexidade())
			.garantiaDias(servico.getGarantiaDias())
			.observacoesTecnicas(servico.getObservacoesTecnicas())
			.ativo(servico.isAtivo())
			.dataCriacao(servico.getDataCriacao())
			.dataUltimaAtualizacao(servico.getDataUltimaAtualizacao())
			.dataRemocao(servico.getDataRemocao())
			.build();
	}

	/**
	 * Atualiza os campos de uma entidade JPA existente a partir do estado atual do
	 * dominio, preservando o identificador e a data de criacao originais.
	 * @param servico agregado de dominio com os dados atualizados.
	 * @param entity entidade JPA a ser atualizada em-place.
	 */
	public void updateEntityFromDomain(Servico servico, ServicoJpaEntity entity) {
		if (servico == null || entity == null) {
			return;
		}
		entity.setNome(servico.getNome());
		entity.setDescricao(servico.getDescricao());
		entity.setValor(servico.getValor());
		entity.setCategoria(servico.getCategoria());
		entity.setNivelComplexidade(servico.getNivelComplexidade());
		entity.setGarantiaDias(servico.getGarantiaDias());
		entity.setObservacoesTecnicas(servico.getObservacoesTecnicas());
		entity.setAtivo(servico.isAtivo());
		entity.setDataUltimaAtualizacao(servico.getDataUltimaAtualizacao());
		entity.setDataRemocao(servico.getDataRemocao());
	}

	/**
	 * Converte uma entidade JPA em agregado de dominio para uso nas camadas de negocio.
	 * @param entity entidade JPA lida do banco de dados.
	 * @return agregado de dominio reconstituido.
	 */
	public Servico toDomain(ServicoJpaEntity entity) {
		if (entity == null) {
			return null;
		}
		return new Servico(entity.getId(), entity.getNome(), entity.getDescricao(), entity.getValor(),
				entity.getCategoria(), entity.getNivelComplexidade(), entity.getGarantiaDias(),
				entity.getObservacoesTecnicas(), Boolean.TRUE.equals(entity.getAtivo()), entity.getDataCriacao(),
				entity.getDataUltimaAtualizacao(), entity.getDataRemocao());
	}

}
