# Agente: Migration SQL

## Responsabilidade
Criar arquivos de migration Flyway para novas tabelas ou alterações de schema no projeto workshop-service.

## Contexto do projeto
- Spring Boot 3.4.1 + Flyway + PostgreSQL
- Pasta de migrations: `src/main/resources/db/migration/`
- Convenção de nomenclatura: `V0.{YYYYMMDDHHmmss}__{descricao_em_snake_case}.sql`
- Timestamp deve usar a data/hora atual no formato `YYYYMMDDHHmmss`

## Padrão SQL (baseado nas migrations existentes)

```sql
CREATE TABLE nome_tabela (
    id UUID PRIMARY KEY,
    campo VARCHAR(N) NOT NULL,
    campo_opcional TEXT,
    ativo BOOLEAN NOT NULL,
    data_remocao TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL,
    data_ultima_atualizacao TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX ux_nome_tabela_campo ON nome_tabela (campo) WHERE ativo = TRUE;
CREATE INDEX ix_nome_tabela_ativo ON nome_tabela (ativo);

COMMENT ON TABLE nome_tabela IS '...';
COMMENT ON COLUMN nome_tabela.id IS 'Identificador unico em formato UUID.';
```

## Regras obrigatórias
- Todo campo obrigatório usa `NOT NULL`
- Campos com unicidade condicional (ex: nome ativo único) usam `UNIQUE INDEX ... WHERE ativo = TRUE`
- Sempre incluir `COMMENT ON TABLE` e `COMMENT ON COLUMN` para todos os campos
- Enums do domínio são armazenados como `VARCHAR` no banco (sem tipo ENUM do PostgreSQL)
- FKs nomeadas seguindo padrão: `fk_{tabela_origem}_{tabela_destino}`
- PKs compostas nomeadas: `pk_{tabela}`
- Índices nomeados: `ux_` para unique, `ix_` para comum

## Como usar este agente
Ao receber uma tarefa de migration, o agente deve:
1. Ler as migrations existentes em `src/main/resources/db/migration/` para entender o padrão atual
2. Criar o arquivo seguindo a nomenclatura correta com timestamp da data atual
3. Garantir que o SQL está compatível com PostgreSQL
4. Validar que todos os campos obrigatórios do domínio estão presentes
