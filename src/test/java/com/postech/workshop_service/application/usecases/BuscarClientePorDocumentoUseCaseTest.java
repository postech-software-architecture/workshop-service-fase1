package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.domain.valueobjects.Documento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarClientePorDocumentoUseCaseTest {

	@Mock
	private ClienteRepository clienteRepository;

	@InjectMocks
	private BuscarClientePorDocumentoUseCase useCase;

	@Test
	void shouldFindByDocument() {
		String doc = "987.654.321-00";
		String cleanDoc = "98765432100";
		Cliente cliente = new Cliente(null, "Nome", new Documento(cleanDoc), "e@e.com", null);
		when(clienteRepository.buscarPorDocumento(cleanDoc)).thenReturn(Optional.of(cliente));

		Optional<Cliente> result = useCase.executar(doc);

		assertTrue(result.isPresent());
		assertEquals(cleanDoc, result.get().getDocumento().getValor());
	}

}
