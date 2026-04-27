# Quickstart: Gestao de Veiculos de Clientes

## Objetivo

Implementar a gestao completa de veiculos com vinculo obrigatorio a um ou mais clientes existentes, validacao de placa, consultas operacionais paginadas e remocao logica com preservacao de historico.

## Passos de implementacao

1. Criar migration Flyway para `veiculos` e `veiculos_clientes`, com comentarios SQL, PK UUID, auditoria obrigatoria, FKs nomeadas, campo de soft delete e indice de unicidade operacional para placa.
2. Adicionar entidade de dominio `Veiculo` e value objects `Placa`, `Chassi` e `Renavam`, encapsulando validacoes e comportamento de atualizacao/remocao.
3. Criar contrato de repositorio de dominio para veiculos e adaptar a persistencia JPA com entidades/mappers para o agregado e sua associacao com clientes.
4. Implementar casos de uso para criar, buscar por ID, buscar por placa, listar com filtros, listar por cliente, atualizar e remover logicamente.
5. Validar no fluxo de aplicacao que todos os `clienteIds` existem e que o conjunto final de clientes vinculados nunca fica vazio.
6. Expor controller e DTOs em pt-BR com validacoes estruturais, Javadocs e documentacao OpenAPI alinhada ao contrato em `contracts/openapi.yaml`.
7. Manter os endpoints liberados no MVP, deixando a configuracao preparada para futura restricao de acesso.
8. Cobrir a feature com testes unitarios para dominio e casos de uso, e testes de integracao para controller/repositorio com PostgreSQL real via Testcontainers.

## Verificacao local

1. Executar `mvn test` para validar testes unitarios, integracao e cobertura configurada.
2. Confirmar que a migration sobe corretamente em ambiente local com PostgreSQL e nos testes com Testcontainers.
3. Validar os fluxos principais:
   - Criar veiculo com dois clientes existentes.
   - Consultar por ID, placa com e sem mascara, e por cliente.
   - Atualizar quilometragem, observacoes e lista de clientes mantendo ao menos um vinculo.
   - Remover logicamente o veiculo e confirmar ausencia nas consultas operacionais padrao.
   - Reutilizar a mesma placa em novo cadastro somente apos a inativacao do registro anterior.
4. Verificar os codigos HTTP esperados:
   - `201` para criacao
   - `200` para consultas e atualizacao
   - `204` para remocao logica
   - `400` para regras de negocio
   - `404` para recurso inexistente
   - `422` para validacao estrutural

## Dados minimos de exemplo

- Clientes existentes com IDs validos `clienteA` e `clienteB`.
- Veiculo novo com placa `BRA1D23`, marca `Toyota`, modelo `Corolla`, ano `2020`.
- Veiculo legado com placa `ABC1234` para validar compatibilidade com o padrao antigo.
