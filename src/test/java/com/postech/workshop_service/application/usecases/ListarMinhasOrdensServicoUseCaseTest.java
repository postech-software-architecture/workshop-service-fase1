package com.postech.workshop_service.application.usecases;

import com.postech.workshop_service.application.exceptions.AcessoNegadoException;
import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.entities.StatusOrdemServico;
import com.postech.workshop_service.domain.repositories.FiltrosOrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.domain.repositories.PaginaResultado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarMinhasOrdensServicoUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@Mock
	private BuscarUsuarioAutenticadoUseCase buscarUsuarioAutenticadoUseCase;

	@InjectMocks
	private ListarMinhasOrdensServicoUseCase useCase;

	@Test
	void deveForcarIdClienteDoPrincipalAutenticadoNoFiltro() {
		UUID clienteId = UUID.randomUUID();
		when(buscarUsuarioAutenticadoUseCase.obterClienteIdObrigatorio()).thenReturn(clienteId);
		PaginaResultado<OrdemServico> pagina = new PaginaResultado<>(List.of(), 0, 0, 0, 20);
		ArgumentCaptor<FiltrosOrdemServico> captor = ArgumentCaptor.forClass(FiltrosOrdemServico.class);
		when(ordemServicoRepository.listar(org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq(20),
				captor.capture()))
			.thenReturn(pagina);

		useCase.executar(0, 20, StatusOrdemServico.AGUARDANDO_APROVACAO);

		FiltrosOrdemServico filtros = captor.getValue();
		assertThat(filtros.idCliente()).isEqualTo(clienteId);
		assertThat(filtros.status()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
		assertThat(filtros.dataInicio()).isNull();
		assertThat(filtros.dataFim()).isNull();
	}

	@Test
	void devePropagarAcessoNegadoQuandoUsuarioSemClienteVinculado() {
		when(buscarUsuarioAutenticadoUseCase.obterClienteIdObrigatorio())
			.thenThrow(new AcessoNegadoException("sem cliente"));

		assertThrows(AcessoNegadoException.class, () -> useCase.executar(0, 20, null));
	}

}
