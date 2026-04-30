package com.postech.workshop_service.infrastructure.persistence.repositories;

import com.postech.workshop_service.domain.entities.Cliente;
import com.postech.workshop_service.domain.repositories.ClienteRepository;
import com.postech.workshop_service.infrastructure.persistence.entities.ClienteJpaEntity;
import com.postech.workshop_service.infrastructure.persistence.mappers.ClienteMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Transactional
public class ClienteRepositoryImpl implements ClienteRepository {

	private final JpaClienteRepository jpaClienteRepository;

	private final ClienteMapper clienteMapper;

	public ClienteRepositoryImpl(JpaClienteRepository jpaClienteRepository, ClienteMapper clienteMapper) {
		this.jpaClienteRepository = jpaClienteRepository;
		this.clienteMapper = clienteMapper;
	}

	@Override
	public Cliente salvar(Cliente cliente) {
		ClienteJpaEntity entity;
		if (cliente.getId() != null) {
			entity = jpaClienteRepository.findById(cliente.getId()).map(existing -> {
				clienteMapper.updateEntityFromDomain(cliente, existing);
				return existing;
			}).orElseGet(() -> clienteMapper.toEntity(cliente));
		}
		else {
			entity = clienteMapper.toEntity(cliente);
		}

		ClienteJpaEntity saved = jpaClienteRepository.save(entity);
		return clienteMapper.toDomain(saved);
	}

	@Override
	public Optional<Cliente> buscarPorId(UUID id, boolean incluirInativos) {
		return jpaClienteRepository.findById(id)
			.filter(entity -> incluirInativos || entity.isAtivo())
			.map(clienteMapper::toDomain);
	}

	@Override
	public Optional<Cliente> buscarPorDocumento(String documento, boolean incluirInativos) {
		if (incluirInativos) {
			return jpaClienteRepository.findByDocumento(documento).map(clienteMapper::toDomain);
		}
		return jpaClienteRepository.findByDocumentoAndAtivoTrue(documento).map(clienteMapper::toDomain);
	}

	@Override
	public List<Cliente> listar(int pagina, int tamanho, boolean incluirInativos) {
		Specification<ClienteJpaEntity> spec = (root, query, cb) -> {
			if (incluirInativos) {
				return cb.conjunction();
			}
			return cb.isTrue(root.get("ativo"));
		};
		Page<ClienteJpaEntity> resultado = jpaClienteRepository.findAll(spec, PageRequest.of(pagina, tamanho));
		return resultado.getContent().stream().map(clienteMapper::toDomain).collect(Collectors.toList());
	}

	@Override
	public long contarTodos() {
		return jpaClienteRepository.count();
	}

	@Override
	public void remover(UUID id) {
		jpaClienteRepository.deleteById(id);
	}

	@Override
	public boolean existePorDocumento(String documento) {
		return jpaClienteRepository.existsByDocumento(documento);
	}

}
