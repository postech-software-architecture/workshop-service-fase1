package com.postech.workshop_service.domain.valueobjects;

/**
 * Tempo medio de execucao agrupado pela descricao do item de servico presente na
 * composicao tecnica das ordens de servico.
 *
 * @param descricaoServico descricao do item de composicao do tipo SERVICO.
 * @param totalExecucoes numero de ordens finalizadas que continham este tipo de servico.
 * @param tempoMedioMinutos media aritmetica do tempo de execucao em minutos.
 */
public record TempoMedioPorTipoServico(String descricaoServico, long totalExecucoes, double tempoMedioMinutos) {
}
