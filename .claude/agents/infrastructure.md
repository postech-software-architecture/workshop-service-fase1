# Agente: Infrastructure Layer

## Responsabilidade
Criar a camada de persistência: JPA entities, mappers, Spring Data repositories e implementações do repositório de domínio.

## Contexto do projeto
- Java 21 + Spring Boot 3.4.1 + Spring Data JPA + MapStruct 1.6.3 + Lombok
- Pacote base: `com.postech.workshop_service`
- Caminho: `src/main/java/com/postech/workshop_service/infrastructure/persistence/`
- Subpacotes: `entities/`, `mappers/`, `repositories/`

## Padrões obrigatórios

### JPA Entity (`entities/`)
- Anotações: `@Entity`, `@Table(name = "nome_tabela")`, `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@Id` com `@Column(name = "id", updatable = false, nullable = false)`
- Enums do domínio mapeados como `@Enumerated(EnumType.STRING)`
- Campos nullable explicitamente marcados com `@Column(nullable = false)` nos obrigatórios
- Relacionamentos com cascade e fetch type explícitos

### Mapper (`mappers/`)
- Classe `@Component`, sem MapStruct `@Mapper` (o projeto usa mapper manual, não gerado)
- Métodos: `toEntity(DomainEntity)`, `toDomain(JpaEntity)`, `updateEntityFromDomain(DomainEntity, JpaEntity)`
- Converte enums diretamente (mesmo nome entre domínio e JPA)
- Javadoc em todos os métodos públicos

### Spring Data Repository (`repositories/Jpa*Repository.java`)
- Interface extende `JpaRepository<Entity, UUID>` e `JpaSpecificationExecutor<Entity>`
- Queries derivadas por convenção de nome quando possível
- `@Query` apenas quando necessário

### Repository Impl (`repositories/*RepositoryImpl.java`)
- Implementa a interface do domínio
- Anotações: `@Component`, `@Transactional`
- Métodos de leitura: `@Transactional(readOnly = true)`
- Filtros dinâmicos usando `Specification<T>` do Spring Data JPA
- Javadoc em todos os métodos públicos com `{@inheritDoc}` quando implementando interface

## Exemplo de Specification

```java
private Specification<MinhaJpaEntity> filtrarPor(String nome, boolean incluirInativos) {
    return (root, query, cb) -> {
        var predicates = cb.conjunction();
        if (!incluirInativos) {
            predicates = cb.and(predicates, cb.isTrue(root.get("ativo")));
        }
        if (nome != null) {
            predicates = cb.and(predicates, cb.like(cb.lower(root.get("nome")), "%" + nome.toLowerCase() + "%"));
        }
        return predicates;
    };
}
```

## Como usar este agente
1. Ler `VeiculoJpaEntity`, `VeiculoMapper` e `VeiculoRepositoryImpl` antes de criar novos arquivos
2. Criar os 4 arquivos: JpaEntity + Mapper + JpaRepository + RepositoryImpl
3. Garantir que os tipos usados no mapper correspondem exatamente aos do domínio
4. Não criar use cases, DTOs ou controllers
