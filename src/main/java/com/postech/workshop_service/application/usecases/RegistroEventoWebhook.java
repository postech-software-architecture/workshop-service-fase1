package com.postech.workshop_service.application.usecases;

/**
 * Porta para idempotencia de eventos de webhook: registra um {@code idEvento} como
 * processado, de forma atomica, para rejeitar reentregas do mesmo evento.
 */
public interface RegistroEventoWebhook {

	/**
	 * Tenta registrar o evento como processado.
	 * @param idEvento identificador unico do evento (pode ser nulo/vazio — nesse caso nao
	 * ha dedup e retorna {@code true}).
	 * @param origem origem/canal do evento (informativo).
	 * @return {@code true} se o evento foi registrado agora (primeira vez); {@code false}
	 * se ja havia sido processado (reentrega).
	 */
	boolean registrarSeInedito(String idEvento, String origem);

}
