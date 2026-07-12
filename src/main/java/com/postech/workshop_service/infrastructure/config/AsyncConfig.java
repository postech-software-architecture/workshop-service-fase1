package com.postech.workshop_service.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Habilita execucao assincrona e define o executor usado pelas notificacoes de mudanca de
 * status (envio fora da thread de request e apos o commit da transacao).
 */
@Configuration
@EnableAsync
public class AsyncConfig {

	/**
	 * Executor dedicado a notificacoes. Pool limitado com fila, para nao criar uma thread
	 * por notificacao nem competir com o pool de requests.
	 * @return executor de notificacoes.
	 */
	@Bean(name = "notificacaoExecutor")
	public TaskExecutor notificacaoExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(4);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("notif-");
		executor.initialize();
		return executor;
	}

}
