package com.postech.workshop_service.application.usecases;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListarOrdensServicoUseCaseTest {

	@Mock
	private OrdemServicoRepository ordemServicoRepository;

	@InjectMocks
	private ListarOrdensServicoUseCase useCase;

	@Test
	void devePassarFiltrosVaziosQuandoNenhumCriterioInformado() {
		PaginaResultado<OrdemServico> pagina = new PaginaResultado<>(List.of(), 0, 0, 0, 20);
		ArgumentCaptor<FiltrosOrdemServico> captor = ArgumentCaptor.forClass(FiltrosOrdemServico.class);
		when(ordemServicoRepository.listar(org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.eq(20),
				captor.capture()))
			.thenReturn(pagina);

		PaginaResultado<OrdemServico> resultado = useCase.executar(0, 20, null, null, null, null);

		assertThat(resultado).isSameAs(pagina);
		FiltrosOrdemServico filtros = captor.getValue();
		assertThat(filtros.status()).isNull();
		assertThat(filtros.idCliente()).isNull();
		assertThat(filtros.dataInicio()).isNull();
		assertThat(filtros.dataFim()).isNull();
	}

	@Test
	void deveCombinarTodosOsFiltrosNoCriterio() {
		UUID idCliente = UUID.randomUUID();
		LocalDateTime dataInicio = LocalDateTime.of(2026, 1, 1, 0, 0);
		LocalDateTime dataFim = LocalDateTime.of(2026, 12, 31, 23, 59);
		PaginaResultado<OrdemServico> pagina = new PaginaResultado<>(List.of(), 0, 0, 1, 10);
		ArgumentCaptor<FiltrosOrdemServico> captor = ArgumentCaptor.forClass(FiltrosOrdemServico.class);
		when(ordemServicoRepository.listar(org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.eq(10),
				captor.capture()))
			.thenReturn(pagina);

		useCase.executar(1, 10, StatusOrdemServico.AGUARDANDO_APROVACAO, idCliente, dataInicio, dataFim);

		FiltrosOrdemServico filtros = captor.getValue();
		assertThat(filtros.status()).isEqualTo(StatusOrdemServico.AGUARDANDO_APROVACAO);
		assertThat(filtros.idCliente()).isEqualTo(idCliente);
		assertThat(filtros.dataInicio()).isEqualTo(dataInicio);
		assertThat(filtros.dataFim()).isEqualTo(dataFim);
	}

}
