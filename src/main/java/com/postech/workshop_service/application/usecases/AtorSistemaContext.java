package com.postech.workshop_service.application.usecases;

/**
 * Marca, com escopo de thread, que a operacao corrente e executada por um ator de sistema
 * (integracao maquina-a-maquina), e nao por um usuario autenticado.
 *
 * <p>
 * Usado pelo webhook de decisao de orcamento para sinalizar explicitamente que as
 * transicoes de status disparadas ali devem ser auditadas como "sistema", em vez de
 * inferir isso de um contexto de seguranca vazio. Fora dessa marca, transicoes sem
 * usuario autenticado continuam falhando fechado.
 * </p>
 */
public final class AtorSistemaContext {

	private static final ThreadLocal<String> ATOR = new ThreadLocal<>();

	private AtorSistemaContext() {
	}

	/**
	 * Executa a acao marcando a thread corrente como operada pelo ator de sistema
	 * informado. A marca e sempre limpa ao final, mesmo em caso de excecao.
	 * @param nomeAtor identificacao do ator de sistema (ex.: origem do webhook).
	 * @param acao acao a executar sob a marca.
	 * @param <T> tipo do resultado.
	 * @return o resultado da acao.
	 */
	public static <T> T executarComo(String nomeAtor, java.util.function.Supplier<T> acao) {
		ATOR.set(nomeAtor);
		try {
			return acao.get();
		}
		finally {
			ATOR.remove();
		}
	}

	/**
	 * @return o nome do ator de sistema ativo na thread corrente, se houver.
	 */
	public static java.util.Optional<String> atorAtual() {
		return java.util.Optional.ofNullable(ATOR.get());
	}

}
