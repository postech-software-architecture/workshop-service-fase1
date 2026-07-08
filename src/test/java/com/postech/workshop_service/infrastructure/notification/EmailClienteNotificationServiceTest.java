package com.postech.workshop_service.infrastructure.notification;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailClienteNotificationServiceTest {

	@Mock
	private JavaMailSender mailSender;

	@Mock
	private ClienteRepository clienteRepository;

	@Test
	void shouldSendEmailWithRecipientAndSubjectOnStatusChange() {
		EmailClienteNotificationService service = new EmailClienteNotificationService(mailSender, clienteRepository,
				"no-reply@workshop.local");
		OrdemServico ordemServico = criarOrdemServico();
		Cliente cliente = new Cliente(ordemServico.getIdCliente(), "Joao", new Documento("12345678909"),
				"joao@example.com", "11999999999");
		when(clienteRepository.buscarPorId(ordemServico.getIdCliente(), true)).thenReturn(Optional.of(cliente));

		service.notificarMudancaStatus(ordemServico, StatusOrdemServico.RECEBIDO, StatusOrdemServico.EM_DIAGNOSTICO);

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());
		SimpleMailMessage enviada = captor.getValue();
		assertNotNull(enviada.getTo());
		assertTrue(List.of(enviada.getTo()).contains("joao@example.com"));
		assertTrue(enviada.getSubject().contains(ordemServico.getNumero()));
		assertTrue(enviada.getSubject().contains("EM_DIAGNOSTICO"));
	}

	@Test
	void shouldSkipSendWhenClientHasNoEmail() {
		EmailClienteNotificationService service = new EmailClienteNotificationService(mailSender, clienteRepository,
				"no-reply@workshop.local");
		OrdemServico ordemServico = criarOrdemServico();
		Cliente semEmail = new Cliente(ordemServico.getIdCliente(), "Maria", new Documento("98765432100"), null,
				"11988887777");
		when(clienteRepository.buscarPorId(ordemServico.getIdCliente(), true)).thenReturn(Optional.of(semEmail));

		service.notificarMudancaStatus(ordemServico, StatusOrdemServico.RECEBIDO, StatusOrdemServico.EM_DIAGNOSTICO);

		verify(mailSender, never()).send(any(SimpleMailMessage.class));
	}

	@Test
	void shouldSkipSendWhenClientNotFound() {
		EmailClienteNotificationService service = new EmailClienteNotificationService(mailSender, clienteRepository,
				"no-reply@workshop.local");
		OrdemServico ordemServico = criarOrdemServico();
		when(clienteRepository.buscarPorId(ordemServico.getIdCliente(), true)).thenReturn(Optional.empty());

		service.notificarMudancaStatus(ordemServico, StatusOrdemServico.RECEBIDO, StatusOrdemServico.EM_DIAGNOSTICO);

		verify(mailSender, never()).send(any(SimpleMailMessage.class));
	}

	private OrdemServico criarOrdemServico() {
		return new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
				StatusOrdemServico.EM_DIAGNOSTICO, List.of(), "OS-2026-00001", null, LocalDateTime.now().minusDays(1),
				LocalDateTime.now(), null);
	}

}
