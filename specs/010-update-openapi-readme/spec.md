# Feature Specification: Atualizacao da Documentacao OpenAPI e README

**Feature Branch**: `010-update-openapi-readme`  
**Created**: 2026-05-12  
**Status**: Draft  
**Input**: User description: "Complementar o arquivo openapi yaml, presente na pasta controllers src\main\java\com\postech\workshop_service\api\controllers\openapi.yaml. Revisitar todas as controllers para validar se esta de acordo e atualizar o arquivo mantendo o padrao. Tambem revisar o readme e complementar caso necessario"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Consultar contrato completo das APIs (Priority: P1)

Como pessoa desenvolvedora ou integradora do sistema de oficina, quero consultar uma documentacao OpenAPI completa e coerente com os recursos disponiveis, para entender quais operacoes posso consumir sem precisar inspecionar o codigo-fonte.

**Why this priority**: A documentacao OpenAPI e o principal contrato publico para consumidores da API; se estiver incompleta ou divergente, integracoes e testes manuais ficam sujeitos a erro.

**Independent Test**: Pode ser testado comparando a documentacao publicada com a lista de recursos expostos pelo sistema e confirmando que cada operacao relevante possui metodo, caminho, proposito, entradas, saidas e respostas esperadas.

**Acceptance Scenarios**:

1. **Given** que existem recursos publicados no sistema, **When** a documentacao OpenAPI e revisada, **Then** cada recurso disponivel esta representado com caminho, metodo e descricao consistentes.
2. **Given** que uma operacao aceita parametros, corpo de requisicao ou filtros, **When** a documentacao OpenAPI e consultada, **Then** essas entradas aparecem com nomes, obrigatoriedade e formatos esperados.
3. **Given** que uma operacao retorna sucesso ou erro conhecido, **When** a documentacao OpenAPI e consultada, **Then** os codigos e formatos de resposta estao descritos de forma padronizada.

---

### User Story 2 - Validar consistencia e padrao da documentacao (Priority: P2)

Como mantenedor do projeto, quero que a documentacao mantenha nomenclatura, agrupamento e estrutura consistentes entre dominios, para reduzir ambiguidades e facilitar evolucoes futuras.

**Why this priority**: Mesmo uma documentacao completa pode ser dificil de manter se cada grupo de endpoints usar convencoes diferentes.

**Independent Test**: Pode ser testado revisando os grupos de endpoints e verificando que descricoes, tags, exemplos, schemas e respostas seguem o mesmo padrao textual e estrutural.

**Acceptance Scenarios**:

1. **Given** que existem operacoes de dominios diferentes, **When** a documentacao e revisada, **Then** tags, nomes de operacao, descricoes e schemas usam convencoes consistentes.
2. **Given** que existem respostas de erro comuns, **When** a documentacao e revisada, **Then** elas sao descritas de forma uniforme entre operacoes comparaveis.

---

### User Story 3 - Entender o uso da API pelo README (Priority: P3)

Como nova pessoa desenvolvedora do projeto, quero encontrar no README orientacoes suficientes para localizar e usar a documentacao da API, para iniciar testes e integracoes com menos dependencia de conhecimento previo.

**Why this priority**: O README complementa o contrato OpenAPI ao orientar descoberta, execucao e uso inicial, mas depende da documentacao principal estar correta.

**Independent Test**: Pode ser testado por uma pessoa nova no projeto, que deve conseguir identificar onde esta a documentacao da API, como executa-la ou acessa-la e quais dominios funcionais estao cobertos.

**Acceptance Scenarios**:

1. **Given** que uma pessoa acessa o README sem conhecer o projeto, **When** ela busca informacoes sobre a API, **Then** encontra referencia clara para a documentacao OpenAPI e orientacoes basicas de uso.
2. **Given** que a documentacao OpenAPI foi atualizada, **When** o README e revisado, **Then** nao ha informacoes conflitantes ou desatualizadas sobre os recursos disponiveis.

### Edge Cases

- Controllers ou operacoes internas, tecnicas ou nao destinadas a consumo externo devem ser avaliadas para evitar exposicao indevida na documentacao publica.
- Endpoints com comportamento condicionado por perfil, autenticacao ou estado do recurso devem deixar essas restricoes claras para o consumidor.
- Operacoes com respostas vazias, paginadas, listas, erros de validacao ou conflitos de negocio devem manter contratos compreensiveis e consistentes.
- O README nao deve duplicar todo o contrato da API; ele deve apontar para a fonte correta e resumir apenas o necessario para orientacao inicial.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: A documentacao da API MUST representar todos os endpoints publicos atualmente disponiveis para consumo externo.
- **FR-002**: Cada operacao documentada MUST informar metodo, caminho, agrupamento funcional, descricao objetiva e resultado esperado.
- **FR-003**: Cada operacao com entradas MUST documentar parametros, filtros, identificadores, corpo de requisicao, obrigatoriedade e formatos relevantes.
- **FR-004**: Cada operacao com saidas MUST documentar respostas de sucesso, respostas de erro previsiveis e formatos de retorno quando aplicavel.
- **FR-005**: A documentacao MUST manter padrao consistente de nomes, tags, descricoes, schemas, exemplos e mensagens entre recursos equivalentes.
- **FR-006**: A documentacao MUST refletir restricoes relevantes para uso da API, incluindo autenticacao, autorizacao, validacoes e regras de negocio observaveis pelo consumidor.
- **FR-007**: A revisao MUST identificar divergencias entre a documentacao existente e os comportamentos expostos pelo sistema, corrigindo omissoes, inconsistencias e informacoes obsoletas.
- **FR-008**: O README MUST ser revisado para garantir que descreve onde localizar ou acessar a documentacao da API e como iniciar seu uso.
- **FR-009**: O README MUST ser complementado quando houver lacunas que dificultem descoberta, execucao ou validacao basica da API por uma nova pessoa desenvolvedora.
- **FR-010**: As atualizacoes MUST preservar o estilo e a organizacao ja adotados pela documentacao existente, exceto quando houver inconsistencia que precise ser corrigida.

### Key Entities

- **Contrato de API**: Representa o conjunto de operacoes publicas disponiveis, seus caminhos, metodos, entradas, saidas e respostas.
- **Operacao documentada**: Representa uma acao consumivel por integradores, associada a um recurso funcional e a cenarios de sucesso ou erro.
- **Schema de dados**: Representa estruturas de requisicao e resposta relevantes para consumidores entenderem formatos e campos.
- **Guia do projeto**: Representa as informacoes do README necessarias para descoberta e uso inicial da API.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% dos endpoints publicos destinados a consumidores externos estao representados na documentacao revisada.
- **SC-002**: 100% das operacoes documentadas possuem metodo, caminho, descricao, respostas de sucesso e respostas de erro aplicaveis.
- **SC-003**: Uma pessoa nova no projeto consegue localizar a documentacao da API e iniciar uma validacao basica em ate 10 minutos usando o README.
- **SC-004**: A revisao nao identifica informacoes conflitantes entre README e documentacao da API apos a atualizacao.
- **SC-005**: Pelo menos 95% das operacoes equivalentes seguem o mesmo padrao de agrupamento, nomenclatura e descricao, com excecoes justificadas por diferencas reais de comportamento.

## Assumptions

- A fonte de verdade para endpoints disponiveis e o conjunto atual de controllers do projeto.
- O escopo desta feature e documental: atualizar o contrato OpenAPI e o README sem alterar comportamento funcional da API.
- Endpoints publicos sao aqueles destinados a consumo por clientes, integradores, pessoas testadoras ou operadores autorizados.
- O README deve complementar a documentacao da API, nao substituir o contrato detalhado.
- A documentacao deve preservar o idioma e o estilo predominantes ja adotados no projeto.
