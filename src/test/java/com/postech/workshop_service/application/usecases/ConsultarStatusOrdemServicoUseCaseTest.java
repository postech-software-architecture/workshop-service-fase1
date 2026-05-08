package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
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
class ConsultarStatusOrdemServicoUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private BuscarUsuarioAutenticadoUseCase buscarUsuarioAutenticadoUseCase;

	@InjectMocks
	private ConsultarStatusOrdemServicoUseCase useCase;

	@Test
	void deveRetornarOrdemQuandoPertenceAoClienteAutenticado() {
		UUID clienteId = UUID.randomUUID();
		UUID idOrdem = UUID.randomUUID();
		OrdemServico ordem = new OrdemServico(idOrdem, clienteId, UUID.randomUUID());
		when(buscarUsuarioAutenticadoUseCase.obterClienteIdObrigatorio()).thenReturn(clienteId);
		when(ordemServicoRepository.buscarPorId(idOrdem)).thenReturn(Optional.of(ordem));

		OrdemServico resultado = useCase.executar(idOrdem);

		assertThat(resultado).isSameAs(ordem);
	}

	@Test
	void deveLancarRecursoNaoEncontradoQuandoOrdemAusente() {
		UUID idOrdem = UUID.randomUUID();
		when(buscarUsuarioAutenticadoUseCase.obterClienteIdObrigatorio()).thenReturn(UUID.randomUUID());
		when(ordemServicoRepository.buscarPorId(idOrdem)).thenReturn(Optional.empty());

		assertThrows(RecursoNaoEncontradoException.class, () -> useCase.executar(idOrdem));
	}

	@Test
	void deveLancarAcessoNegadoQuandoOrdemPertenceAOutroCliente() {
		UUID clienteAutenticado = UUID.randomUUID();
		UUID outroCliente = UUID.randomUUID();
		UUID idOrdem = UUID.randomUUID();
		OrdemServico ordem = new OrdemServico(idOrdem, outroCliente, UUID.randomUUID());
		when(buscarUsuarioAutenticadoUseCase.obterClienteIdObrigatorio()).thenReturn(clienteAutenticado);
		when(ordemServicoRepository.buscarPorId(idOrdem)).thenReturn(Optional.of(ordem));

		assertThrows(AcessoNegadoException.class, () -> useCase.executar(idOrdem));
	}

}
