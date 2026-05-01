# Agente: Application Layer (Use Cases)

## Responsabilidade
Criar use cases da camada de aplicação — um arquivo por caso de uso, seguindo o princípio de responsabilidade única.

## Contexto do projeto
- Java 21 + Spring Boot 3.4.1
- Pacote: `com.postech.workshop_service.application.usecases`
- Caminho: `src/main/java/com/postech/workshop_service/application/usecases/`
- Exceções disponíveis:
  - `RegraDeNegocioException` → regras de negócio violadas (HTTP 422)
  - `RecursoNaoEncontradoException` → entidade não encontrada (HTTP 404)

## Padrões obrigatórios

### Estrutura do use case
```java
@Service
public class NomeDoUseCase {

    private final AlgumRepository algumRepository;

    public NomeDoUseCase(AlgumRepository algumRepository) {
        this.algumRepository = algumRepository;
    }

    @Transactional
    public EntidadeDominio executar(/* parâmetros */) {
        try {
            // lógica de negócio
        } catch (RegraDeNegocioException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw new RegraDeNegocioException(ex.getMessage());
        }
    }
}
```

### Regras de nomenclatura
- Uma classe por ação: `CriarXUseCase`, `AtualizarXUseCase`, `BuscarXPorYUseCase`, `ListarXUseCase`, `RemoverXUseCase`
- Método sempre chamado `executar(...)`
- Use cases de leitura usam `@Transactional(readOnly = true)` ou sem `@Transactional`

### Validações no use case
- Unicidade (ex: nome duplicado) → verificar via repositório antes de salvar
- Existência de dependências (ex: cliente existe?) → verificar e lançar `RegraDeNegocioException` se não encontrado
- Regras que envolvem múltiplos agregados ficam no use case, não na entidade
- `IllegalArgumentException` da entidade de domínio é sempre convertida para `RegraDeNegocioException`

### Use cases de leitura
```java
@Service
public class BuscarXPorIdUseCase {
    public Optional<X> executar(UUID id) {
        return xRepository.buscarPorId(id);
    }
}
```

## Como usar este agente
1. Ler um use case existente (ex: `CriarVeiculoUseCase`, `AtualizarVeiculoUseCase`) antes de criar novos
2. Criar um arquivo por use case — nunca agrupar múltiplas ações em uma classe
3. Não criar DTOs, controllers, entidades JPA ou mappers
4. Os parâmetros do método `executar` são tipos primitivos/domínio, nunca DTOs da camada API
