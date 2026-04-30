package com.postech.workshop_service.infrastructure.persistence.entities;

import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade JPA da ordem de servico.
 */
@Entity
@Table(name = "ordens_servico")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrdemServicoJpaEntity extends BaseJpaEntity {

	@Column(name = "id_cliente", nullable = false)
	private UUID idCliente;

	@Column(name = "id_veiculo", nullable = false)
	private UUID idVeiculo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private StatusOrdemServico status;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "ordens_servico_itens", joinColumns = @JoinColumn(name = "ordem_servico_id"),
			foreignKey = @ForeignKey(name = "fk_ordens_servico_itens_ordem_servico"))
	@OrderColumn(name = "ordem_item")
	private List<ItemComposicaoTecnicaJpaEntity> itensComposicao = new ArrayList<>();

}
