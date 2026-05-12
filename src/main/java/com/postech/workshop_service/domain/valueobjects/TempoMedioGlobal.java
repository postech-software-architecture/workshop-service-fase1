package com.postech.workshop_service.domain.valueobjects;

/**
 * Resultado agregado do tempo medio de execucao das ordens de servico.
 *
 * @param totalOrdens total de ordens finalizadas ou entregues consideradas no calculo.
 * @param tempoMedioMinutos media aritmetica do tempo de execucao em minutos.
 * @param tempoMinimoMinutos menor tempo de execucao registrado em minutos.
 * @param tempoMaximoMinutos maior tempo de execucao registrado em minutos.
 */
public record TempoMedioGlobal(long totalOrdens, double tempoMedioMinutos, double tempoMinimoMinutos,
		double tempoMaximoMinutos) {
}
