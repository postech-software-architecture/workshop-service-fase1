package com.postech.workshop_service.application.usecases;

import java.util.List;

/** Dados de aplicacao para abertura de uma ordem de servico com itens iniciais. */
public record DadosCriacaoOrdemServicoComItens(String clienteDocumento, String veiculoPlaca, String veiculoMarca,
		String veiculoModelo, Integer veiculoAno, List<ItemServicoSolicitado> servicos, List<ItemPecaSolicitada> pecas,
		String observacoes) {
}
