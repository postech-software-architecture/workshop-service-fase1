package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.RecursoNaoEncontradoException;
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
class BuscarOrdemServicoPorIdUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@InjectMocks
	private BuscarOrdemServicoPorIdUseCase useCase;

	@Test
	void deveRetornarOrdemQuandoEncontrada() {
		UUID id = UUID.randomUUID();
		OrdemServico ordem = new OrdemServico(id, UUID.randomUUID(), UUID.randomUUID());
		when(ordemServicoRepository.buscarPorId(id)).thenReturn(Optional.of(ordem));

		OrdemServico resultado = useCase.executar(id);

		assertThat(resultado).isSameAs(ordem);
	}

	@Test
	void deveLancarRecursoNaoEncontradoQuandoOrdemAusente() {
		UUID id = UUID.randomUUID();
		when(ordemServicoRepository.buscarPorId(id)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(id));
	}

}
