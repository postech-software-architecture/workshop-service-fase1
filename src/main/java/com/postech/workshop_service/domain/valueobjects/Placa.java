package com.postech.workshop_service.domain.valueobjects;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Objeto de valor que representa a placa normalizada de um veiculo.
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Placa {

	private static final String PADRAO_ANTIGO = "^[A-Z]{3}[0-9]{4}$";

	private static final String PADRAO_MERCOSUL = "^[A-Z]{3}[0-9][A-Z][0-9]{2}$";

	@EqualsAndHashCode.Include
	private final String valor;

	/**
	 * Cria uma nova placa validando e normalizando o valor informado.
	 * @param valorOriginal valor bruto informado pelo usuario.
	 */
	public Placa(String valorOriginal) {
		if (valorOriginal == null || valorOriginal.trim().isEmpty()) {
			throw new IllegalArgumentException("A placa do veiculo e obrigatoria.");
		}

		String normalizada = valorOriginal.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
		if (!normalizada.matches(PADRAO_ANTIGO) && !normalizada.matches(PADRAO_MERCOSUL)) {
			throw new IllegalArgumentException("A placa informada nao corresponde aos formatos aceitos.");
		}

		this.valor = normalizada;
	}

}
