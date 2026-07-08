package com.postech.workshop_service.domain.entities;

import com.postech.workshop_service.domain.exceptions.RegraDeNegocioException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Entidade raiz que representa uma ordem de servico da oficina.
 */
@Getter
public class OrdemServico extends EntidadeBase {

	private final UUID idCliente;

	private final UUID idVeiculo;

	private StatusOrdemServico status;

	private List<ItemComposicaoTecnica> itensComposicao;

	private final String numero;

	private final String observacoes;

	private LocalDateTime dataInicioExecucao;

	private LocalDateTime dataFinalizacao;

	private LocalDateTime dataEntrega;

	private Orcamento orcamentoAtual;

	/**
	 * Vincula o orcamento atual a esta ordem de servico para fins de consulta.
	 * @param orcamento orcamento a ser vinculado.
	 */
	public void vincularOrcamento(Orcamento orcamento) {
		this.orcamentoAtual = orcamento;
	}

	/**
	 * Cria uma nova ordem de servico vazia, em composicao, sem numero definido. Usado
	 * internamente e em testes de dominio.
	 * @param id identificador tecnico; nulo gera UUID automatico.
	 * @param idCliente identificador do cliente vinculado.
	 * @param idVeiculo identificador do veiculo vinculado.
	 */
	public OrdemServico(UUID id, UUID idCliente, UUID idVeiculo) {
		super(id != null ? id : UUID.randomUUID());
		this.idCliente = validarIdentificador(idCliente, "O identificador do cliente e obrigatorio.");
		this.idVeiculo = validarIdentificador(idVeiculo, "O identificador do veiculo e obrigatorio.");
		this.status = StatusOrdemServico.RECEBIDO;
		this.itensComposicao = List.of();
		this.numero = null;
		this.observacoes = null;
		this.dataInicioExecucao = null;
		this.dataFinalizacao = null;
		this.dataEntrega = null;
	}

	/**
	 * Cria uma nova ordem de servico na recepcao do veiculo, com composicao tecnica ja
	 * definida e numero sequencial gerado.
	 * @param id identificador tecnico; nulo gera UUID automatico.
	 * @param idCliente identificador do cliente vinculado.
	 * @param idVeiculo identificador do veiculo vinculado.
	 * @param numero numero sequencial unico no formato OS-{ANO}-{NNNNN}.
	 * @param observacoes observacoes opcionais do atendente.
	 * @param itensComposicao itens de servicos e pecas ja levantados.
	 */
	public OrdemServico(UUID id, UUID idCliente, UUID idVeiculo, String numero, String observacoes,
			Collection<ItemComposicaoTecnica> itensComposicao) {
		super(id != null ? id : UUID.randomUUID());
		this.idCliente = validarIdentificador(idCliente, "O identificador do cliente e obrigatorio.");
		this.idVeiculo = validarIdentificador(idVeiculo, "O identificador do veiculo e obrigatorio.");
		this.numero = validarNumero(numero);
		this.observacoes = sanitizarOpcional(observacoes);
		this.status = StatusOrdemServico.RECEBIDO;
		this.itensComposicao = validarItensComposicao(itensComposicao);
		this.dataInicioExecucao = null;
		this.dataFinalizacao = null;
		this.dataEntrega = null;
	}

	/**
	 * Reconstroi uma ordem de servico previamente persistida.
	 * @param id identificador tecnico da ordem de servico.
	 * @param idCliente identificador do cliente vinculado.
	 * @param idVeiculo identificador do veiculo vinculado.
	 * @param status estado atual da ordem de servico.
	 * @param itensComposicao itens da composicao tecnica.
	 * @param numero numero sequencial da OS.
	 * @param observacoes observacoes registradas na recepcao.
	 * @param dataCriacao data de criacao.
	 * @param dataUltimaAtualizacao data da ultima atualizacao.
	 * @param dataRemocao data da remocao logica.
	 */
	public OrdemServico(UUID id, UUID idCliente, UUID idVeiculo, StatusOrdemServico status,
			Collection<ItemComposicaoTecnica> itensComposicao, String numero, String observacoes,
			LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao, LocalDateTime dataRemocao) {
		this(id, idCliente, idVeiculo, status, itensComposicao, numero, observacoes, null, null, null, dataCriacao,
				dataUltimaAtualizacao, dataRemocao);
	}

	/**
	 * Reconstroi uma ordem de servico previamente persistida com dados do ciclo de
	 * execucao.
	 * @param id identificador tecnico da ordem de servico.
	 * @param idCliente identificador do cliente vinculado.
	 * @param idVeiculo identificador do veiculo vinculado.
	 * @param status estado atual da ordem de servico.
	 * @param itensComposicao itens da composicao tecnica.
	 * @param numero numero sequencial da OS.
	 * @param observacoes observacoes registradas na recepcao.
	 * @param dataInicioExecucao data de inicio da execucao tecnica.
	 * @param dataFinalizacao data de finalizacao tecnica.
	 * @param dataEntrega data de entrega ao cliente.
	 * @param dataCriacao data de criacao.
	 * @param dataUltimaAtualizacao data da ultima atualizacao.
	 * @param dataRemocao data da remocao logica.
	 */
	@Default
	public OrdemServico(UUID id, UUID idCliente, UUID idVeiculo, StatusOrdemServico status,
			Collection<ItemComposicaoTecnica> itensComposicao, String numero, String observacoes,
			LocalDateTime dataInicioExecucao, LocalDateTime dataFinalizacao, LocalDateTime dataEntrega,
			LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao, LocalDateTime dataRemocao) {
		super(id, dataCriacao, dataUltimaAtualizacao, dataRemocao);
		this.idCliente = validarIdentificador(idCliente, "O identificador do cliente e obrigatorio.");
		this.idVeiculo = validarIdentificador(idVeiculo, "O identificador do veiculo e obrigatorio.");
		this.status = validarStatus(status);
		this.itensComposicao = validarItensComposicao(itensComposicao);
		this.numero = numero;
		this.observacoes = sanitizarOpcional(observacoes);
		this.dataInicioExecucao = dataInicioExecucao;
		this.dataFinalizacao = dataFinalizacao;
		this.dataEntrega = dataEntrega;
	}

	/**
	 * Inicia o diagnostico do veiculo apos a recepcao.
	 */
	public void iniciarDiagnostico() {
		if (this.status != StatusOrdemServico.RECEBIDO) {
			throw new RegraDeNegocioException(
					"Nao e permitido iniciar diagnostico de uma ordem de servico com status " + this.status + ".");
		}
		this.status = StatusOrdemServico.EM_DIAGNOSTICO;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Encerra o diagnostico e avanca para a fase de composicao tecnica.
	 */
	public void encerrarDiagnostico() {
		if (this.status != StatusOrdemServico.EM_DIAGNOSTICO) {
			throw new RegraDeNegocioException(
					"Nao e permitido encerrar diagnostico de uma ordem de servico com status " + this.status + ".");
		}
		this.status = StatusOrdemServico.EM_COMPOSICAO;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Adiciona um item a composicao tecnica da ordem de servico.
	 * @param item item a ser adicionado.
	 */
	public void adicionarItem(ItemComposicaoTecnica item) {
		if (this.status != StatusOrdemServico.EM_COMPOSICAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido adicionar itens a uma ordem de servico com status " + this.status + ".");
		}
		if (item == null) {
			throw new IllegalArgumentException("Nao e permitido informar item nulo na ordem de servico.");
		}
		List<ItemComposicaoTecnica> novaLista = new ArrayList<>(this.itensComposicao);
		novaLista.add(item);
		this.itensComposicao = List.copyOf(novaLista);
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Encerra a composicao tecnica quando houver pelo menos um item cadastrado.
	 */
	public void encerrarComposicao() {
		if (this.status != StatusOrdemServico.EM_COMPOSICAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido encerrar a composicao tecnica de uma ordem de servico com status " + this.status
							+ ".");
		}
		if (this.itensComposicao.isEmpty()) {
			throw new RegraDeNegocioException(
					"Nao e permitido encerrar a composicao tecnica de uma ordem de servico sem itens.");
		}
		this.status = StatusOrdemServico.AGUARDANDO_APROVACAO;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Retorna a ordem de servico para a fase de composicao tecnica.
	 */
	public void voltarParaComposicao() {
		if (this.status != StatusOrdemServico.AGUARDANDO_APROVACAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido voltar para composicao uma ordem de servico com status " + this.status + ".");
		}
		this.status = StatusOrdemServico.EM_COMPOSICAO;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Marca a ordem como aguardando execucao apos aprovacao do orcamento.
	 */
	public void marcarComoAguardandoExecucao() {
		if (this.status != StatusOrdemServico.AGUARDANDO_APROVACAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido marcar como aguardando execucao uma ordem de servico com status " + this.status
							+ ".");
		}
		this.status = StatusOrdemServico.AGUARDANDO_EXECUCAO;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Cancela a ordem de servico quando o estado atual permite essa operacao.
	 * @throws RegraDeNegocioException quando a ordem de servico nao pode ser cancelada.
	 */
	public void cancelar() {
		if (this.status != StatusOrdemServico.AGUARDANDO_APROVACAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido cancelar uma ordem de servico com status " + this.status + ".");
		}
		this.status = StatusOrdemServico.CANCELADA;
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Inicia a execucao tecnica de uma ordem previamente aprovada.
	 */
	public void iniciarExecucao() {
		if (this.status != StatusOrdemServico.AGUARDANDO_EXECUCAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido iniciar execucao de uma ordem de servico com status " + this.status + ".");
		}
		this.status = StatusOrdemServico.EM_EXECUCAO;
		this.dataInicioExecucao = LocalDateTime.now();
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Finaliza a execucao tecnica de uma ordem em andamento. Exige que todos os itens do
	 * tipo SERVICO estejam com status FINALIZADO.
	 */
	public void finalizarExecucao() {
		if (this.status != StatusOrdemServico.EM_EXECUCAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido finalizar execucao de uma ordem de servico com status " + this.status + ".");
		}
		for (ItemComposicaoTecnica item : this.itensComposicao) {
			if (item.isServico() && !item.estaFinalizado()) {
				throw new RegraDeNegocioException(
						"Nao e possivel finalizar a OS: existem servicos pendentes ou em execucao.");
			}
		}
		this.status = StatusOrdemServico.FINALIZADA;
		this.dataFinalizacao = LocalDateTime.now();
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Inicia a execucao de um item de servico individual da composicao tecnica. Apenas um
	 * item de servico pode estar em execucao simultaneamente nesta ordem de servico.
	 * @param idItem identificador do item de servico a ser iniciado.
	 */
	public void iniciarServico(UUID idItem) {
		if (this.status != StatusOrdemServico.EM_EXECUCAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido iniciar um servico em uma ordem de servico com status " + this.status + ".");
		}
		ItemComposicaoTecnica item = buscarItemPorId(idItem);
		if (!item.isServico()) {
			throw new RegraDeNegocioException("O item informado nao e um servico.");
		}
		if (item.getStatusExecucao() != StatusItemExecucao.PENDENTE) {
			throw new RegraDeNegocioException("O servico ja foi iniciado ou finalizado.");
		}
		for (ItemComposicaoTecnica outro : this.itensComposicao) {
			if (outro.isServico() && outro.estaEmExecucao() && !outro.getId().equals(item.getId())) {
				throw new RegraDeNegocioException(
						"Ja existe um servico em execucao nesta ordem. Finalize-o antes de iniciar outro.");
			}
		}
		item.marcarInicioExecucao();
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Finaliza a execucao de um item de servico individual da composicao tecnica.
	 * @param idItem identificador do item de servico a ser finalizado.
	 */
	public void finalizarServico(UUID idItem) {
		if (this.status != StatusOrdemServico.EM_EXECUCAO) {
			throw new RegraDeNegocioException(
					"Nao e permitido finalizar um servico em uma ordem de servico com status " + this.status + ".");
		}
		ItemComposicaoTecnica item = buscarItemPorId(idItem);
		if (!item.isServico()) {
			throw new RegraDeNegocioException("O item informado nao e um servico.");
		}
		if (item.getStatusExecucao() != StatusItemExecucao.EM_EXECUCAO) {
			throw new RegraDeNegocioException("O servico nao esta em execucao.");
		}
		item.marcarFinalizacao();
		atualizarDataUltimaAtualizacao();
	}

	private ItemComposicaoTecnica buscarItemPorId(UUID idItem) {
		if (idItem == null) {
			throw new RegraDeNegocioException("Item de servico nao encontrado na ordem de servico.");
		}
		for (ItemComposicaoTecnica item : this.itensComposicao) {
			if (idItem.equals(item.getId())) {
				return item;
			}
		}
		throw new RegraDeNegocioException("Item de servico nao encontrado na ordem de servico.");
	}

	/**
	 * Registra a entrega do veiculo ao cliente apos finalizacao tecnica.
	 */
	public void entregar() {
		if (this.status != StatusOrdemServico.FINALIZADA) {
			throw new RegraDeNegocioException(
					"Nao e permitido entregar uma ordem de servico com status " + this.status + ".");
		}
		this.status = StatusOrdemServico.ENTREGUE;
		this.dataEntrega = LocalDateTime.now();
		atualizarDataUltimaAtualizacao();
	}

	/**
	 * Indica se a ordem possui ao menos um item de composicao tecnica.
	 * @return {@code true} quando houver itens cadastrados.
	 */
	public boolean possuiItensComposicao() {
		return !this.itensComposicao.isEmpty();
	}

	/**
	 * Indica se a ordem de servico pode ser cancelada no estado atual.
	 * @return {@code true} apenas quando o status for
	 * {@link StatusOrdemServico#AGUARDANDO_APROVACAO}.
	 */
	public boolean podeSerCancelada() {
		return this.status == StatusOrdemServico.AGUARDANDO_APROVACAO;
	}

	private UUID validarIdentificador(UUID identificador, String mensagem) {
		if (identificador == null) {
			throw new IllegalArgumentException(mensagem);
		}
		return identificador;
	}

	private StatusOrdemServico validarStatus(StatusOrdemServico status) {
		if (status == null) {
			throw new IllegalArgumentException("O status da ordem de servico e obrigatorio.");
		}
		return status;
	}

	private String validarNumero(String numero) {
		if (numero == null || numero.isBlank()) {
			throw new IllegalArgumentException("O numero da ordem de servico e obrigatorio.");
		}
		return numero.trim();
	}

	private String sanitizarOpcional(String valor) {
		if (valor == null) {
			return null;
		}
		String sanitizado = valor.trim().replaceAll("\\s+", " ");
		return sanitizado.isEmpty() ? null : sanitizado;
	}

	private List<ItemComposicaoTecnica> validarItensComposicao(Collection<ItemComposicaoTecnica> itensComposicao) {
		if (itensComposicao == null) {
			return List.of();
		}

		List<ItemComposicaoTecnica> itensValidados = new ArrayList<>();
		for (ItemComposicaoTecnica item : itensComposicao) {
			if (item == null) {
				throw new IllegalArgumentException("Nao e permitido informar item nulo na ordem de servico.");
			}
			itensValidados.add(item);
		}
		return List.copyOf(itensValidados);
	}

}
