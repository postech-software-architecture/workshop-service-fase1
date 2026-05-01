# Agente: Domain Layer

## Responsabilidade
Criar entidades de domínio, value objects, enums e interfaces de repositório seguindo Clean Architecture e DDD.

## Contexto do projeto
- Java 21, pacote base: `com.postech.workshop_service`
- Caminho: `src/main/java/com/postech/workshop_service/domain/`
- Subpacotes: `entities/`, `valueobjects/`, `enums/`, `repositories/`

## Padrões obrigatórios

### Entidades
- Sem anotações Spring ou JPA (domínio puro)
- Sem Lombok na entidade — apenas código Java explícito
- Dois construtores: um para criação (id nullable → gera UUID) e um para reconstrução (id obrigatório)
- Validações lançam `IllegalArgumentException` com mensagem descritiva
- Sanitização de strings: `trim()` + colapso de espaços duplos; retorna `null` se vazio
- Métodos de negócio explícitos: `atualizarDados(...)`, `removerLogicamente()`
- `removerLogicamente()` é idempotente (não lança erro se já inativo)
- `@Getter` do Lombok pode ser usado apenas em enums
- Javadoc nos construtores e métodos públicos

### Value Objects
- Classe `final`, construtor valida e normaliza o valor
- Campo `valor` privado e final, exposto por getter
- Validações lançam `IllegalArgumentException`

### Interface de Repositório
- Fica em `domain/repositories/`
- Métodos tipados com domínio puro (sem entidades JPA)
- Usa `Optional<T>` para buscas que podem não encontrar
- Usa `PaginaResultado<T>` para listagens paginadas (record existente no projeto)

## Exemplo de estrutura de entidade

```java
@Getter
public class MinhaEntidade {
    private final UUID id;
    private String nome;
    private final LocalDateTime dataCriacao;
    private LocalDateTime dataUltimaAtualizacao;
    private LocalDateTime dataRemocao;
    private boolean ativo;

    // Construtor de criação
    public MinhaEntidade(UUID id, String nome) {
        this.id = id != null ? id : UUID.randomUUID();
        this.dataCriacao = LocalDateTime.now();
        this.dataUltimaAtualizacao = this.dataCriacao;
        this.ativo = true;
        aplicarDados(nome);
    }

    // Construtor de reconstrução
    public MinhaEntidade(UUID id, String nome, boolean ativo,
            LocalDateTime dataCriacao, LocalDateTime dataUltimaAtualizacao, LocalDateTime dataRemocao) {
        this.id = Objects.requireNonNull(id);
        this.dataCriacao = Objects.requireNonNull(dataCriacao);
        this.dataUltimaAtualizacao = Objects.requireNonNull(dataUltimaAtualizacao);
        this.dataRemocao = dataRemocao;
        this.ativo = ativo;
        aplicarDados(nome);
    }

    public void atualizarDados(String nome) {
        aplicarDados(nome);
        this.dataUltimaAtualizacao = LocalDateTime.now();
    }

    public void removerLogicamente() {
        if (!this.ativo) return;
        this.ativo = false;
        this.dataRemocao = LocalDateTime.now();
        this.dataUltimaAtualizacao = this.dataRemocao;
    }

    private void aplicarDados(String nome) {
        this.nome = sanitizarObrigatorio(nome, "O nome e obrigatorio.");
    }

    private String sanitizarObrigatorio(String valor, String mensagem) {
        String s = sanitizarOpcional(valor);
        if (s == null) throw new IllegalArgumentException(mensagem);
        return s;
    }

    private String sanitizarOpcional(String valor) {
        if (valor == null) return null;
        String s = valor.trim().replaceAll("\\s+", " ");
        return s.isEmpty() ? null : s;
    }
}
```

## Como usar este agente
1. Ler as entidades existentes em `domain/entities/` antes de criar uma nova
2. Criar entidade + enums necessários + interface do repositório
3. Não criar nada fora da camada `domain/`
