package com.postech.workshop_service.infrastructure.notification;

import com.postech.workshop_service.application.usecases.ClienteNotificationService;
import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.Orcamento;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Implementacao real da notificacao ao cliente por e-mail, ativa quando
 * {@code notificacao.canal=email} (perfis de container/producao).
 *
 * <p>
 * Reside em {@code infrastructure} porque conhece Spring Mail; a camada de aplicacao
 * continua dependendo apenas da porta {@link ClienteNotificationService}. O e-mail do
 * cliente e resolvido a partir do {@code idCliente} da OS via {@link ClienteRepository}.
 * </p>
 */
@Service
@ConditionalOnProperty(name = "notificacao.canal", havingValue = "email")
public class EmailClienteNotificationService implements ClienteNotificationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailClienteNotificationService.class);

	private final JavaMailSender mailSender;

	private final ClienteRepository clienteRepository;

	private final String remetente;

	/**
	 * Construtor para injecao de dependencias.
	 * @param mailSender componente de envio de e-mail do Spring.
	 * @param clienteRepository repositorio para resolver o e-mail do cliente.
	 * @param remetente endereco remetente configurado.
	 */
	public EmailClienteNotificationService(JavaMailSender mailSender, ClienteRepository clienteRepository,
			@Value("${notificacao.email.remetente:no-reply@workshop.local}") String remetente) {
		this.mailSender = mailSender;
		this.clienteRepository = clienteRepository;
		this.remetente = remetente;
	}

	@Override
	public void notificarOrcamentoPendente(OrdemServico ordemServico, Orcamento orcamento) {
		enviar(ordemServico, "Orcamento da OS " + ordemServico.getNumero() + " aguardando aprovacao",
				"O orcamento da sua ordem de servico " + ordemServico.getNumero()
						+ " esta disponivel e aguarda a sua aprovacao.");
	}

	@Override
	public void notificarMudancaStatus(OrdemServico ordemServico, StatusOrdemServico anterior,
			StatusOrdemServico novo) {
		enviar(ordemServico, "OS " + ordemServico.getNumero() + " - status atualizado: " + novo,
				"Sua ordem de servico " + ordemServico.getNumero() + " mudou de " + anterior + " para " + novo + ".");
	}

	private void enviar(OrdemServico ordemServico, String assunto, String corpo) {
		String destinatario = resolverEmail(ordemServico);
		if (destinatario == null || destinatario.isBlank()) {
			LOGGER.warn("Cliente da OS {} sem e-mail cadastrado; notificacao ignorada.", ordemServico.getNumero());
			return;
		}
		SimpleMailMessage mensagem = new SimpleMailMessage();
		mensagem.setFrom(remetente);
		mensagem.setTo(destinatario);
		mensagem.setSubject(assunto);
		mensagem.setText(corpo);
		mailSender.send(mensagem);
	}

	private String resolverEmail(OrdemServico ordemServico) {
		return clienteRepository.buscarPorId(ordemServico.getIdCliente(), true).map(Cliente::getEmail).orElse(null);
	}

}
