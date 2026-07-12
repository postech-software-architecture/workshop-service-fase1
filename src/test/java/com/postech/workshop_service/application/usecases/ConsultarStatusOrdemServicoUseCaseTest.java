package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.domain.exceptions.RecursoNaoEncontradoException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsultarStatusOrdemServicoUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@InjectMocks
	private ConsultarStatusOrdemServicoUseCase useCase;

	@Test
	void deveRetornarOrdemQuandoNumeroExiste() {
		String numero = "OS-2026-00001";
		OrdemServico ordem = new OrdemServico(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
		when(ordemServicoRepository.buscarPorNumero(numero)).thenReturn(Optional.of(ordem));

		OrdemServico resultado = useCase.executar(numero);

		assertThat(resultado).isSameAs(ordem);
	}

	@Test
	void deveLancarRecursoNaoEncontradoQuandoOrdemNaoExiste() {
		String numero = "OS-2026-99999";
		when(ordemServicoRepository.buscarPorNumero(numero)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(numero));
	}

}
