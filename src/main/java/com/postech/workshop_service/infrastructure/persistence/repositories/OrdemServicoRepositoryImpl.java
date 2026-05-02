package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.OrdemServico;
import com.postech.workshop_service.domain.repositories.OrdemServicoRepository;
import com.postech.workshop_service.infrastructure.persistence.mappers.OrdemServicoMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador de persistencia do agregado de ordem de servico.
 */
@Component
@Transactional
public class OrdemServicoRepositoryImpl implements OrdemServicoRepository {

	private final JpaOrdemServicoRepository jpaOrdemServicoRepository;

	private final OrdemServicoMapper ordemServicoMapper;

	/**
	 * Construtor para injecao das dependencias do adaptador.
	 * @param jpaOrdemServicoRepository repositorio Spring Data.
	 * @param ordemServicoMapper mapper da entidade.
	 */
	public OrdemServicoRepositoryImpl(JpaOrdemServicoRepository jpaOrdemServicoRepository,
			OrdemServicoMapper ordemServicoMapper) {
		this.jpaOrdemServicoRepository = jpaOrdemServicoRepository;
		this.ordemServicoMapper = ordemServicoMapper;
	}

	@Override
	public OrdemServico salvar(OrdemServico ordemServico) {
		return ordemServicoMapper.toDomain(jpaOrdemServicoRepository.save(ordemServicoMapper.toEntity(ordemServico)));
	}

	@Override
	public Optional<OrdemServico> buscarPorId(UUID id) {
		return jpaOrdemServicoRepository.findById(id).map(ordemServicoMapper::toDomain);
	}

	@Override
	public String gerarProximoNumero(int ano) {
		String prefixo = "OS-" + ano + "-%";
		int sequencial = jpaOrdemServicoRepository.buscarProximoSequencial(prefixo);
		return String.format("OS-%d-%05d", ano, sequencial);
	}

}
