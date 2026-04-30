package com.postech.workshop_service.infrastructure.persistence.entities;

import com.postech.workshop_service.domain.entities.StatusOrcamento;
import com.postech.workshop_service.domain.entities.TipoOrcamento;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entidade JPA do orcamento.
 */
@Entity
@Table(name = "orcamentos")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrcamentoJpaEntity extends BaseJpaEntity {

	@Column(name = "id_ordem_servico", nullable = false)
	private UUID idOrdemServico;

	@Column(nullable = false, precision = 19, scale = 2)
	private BigDecimal valor;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private StatusOrcamento status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private TipoOrcamento tipo;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "orcamentos_itens", joinColumns = @JoinColumn(name = "orcamento_id"),
			foreignKey = @ForeignKey(name = "fk_orcamentos_itens_orcamento"))
	@OrderColumn(name = "ordem_item")
	private List<ItemOrcamentoJpaEntity> itens = new ArrayList<>();

}
