# Research: Cadastro de Clientes

## Phase 0: Findings

### Spring Boot DDD Structure

- **Decision**: Adotar os pacotes organizacionais `api`, `application`, `domain`, `infrastructure`.
- **Rationale**: Separa claramente as responsabilidades conforme a constitution do projeto (Camadas). Mantém o domínio agnóstico de frameworks externos, utilizando o Spring Boot majoritariamente em `api` e `infrastructure`. A camada `application` foca nos casos de uso (UseCase).
- **Alternatives considered**: Hexagonal Architecture / Ports and Adapters (Mais complexo, requeriria nomenclatura diferente como `ports`, `adapters` e mais boilerplate para um MVP inicial, optou-se por Onion/Camadas clássico e legível).

### Validação de Documento Genérico (CPF/CNPJ)

- **Decision**: O campo `documento` será recebido na DTO da API e validado no domínio usando um Value Object polimórfico ou que encapsula a lógica que infere o tipo com base no tamanho numérico (11 para CPF, 14 para CNPJ).
- **Rationale**: A especificação de requisitos define que existirá apenas um campo para evitar ambiguidade.
- **Alternatives considered**: Dois endpoints separados (descartado por complicar clientes).
