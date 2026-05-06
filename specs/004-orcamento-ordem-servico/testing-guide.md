# Guia de Testes — Criação de Ordem de Serviço

**Feature Branch**: `004-criacao-os`  
**Endpoint**: `POST /api/v1/ordens-servico`  
**Público-alvo**: Dev sênior testando via backend (sem UI)

---

## 1. Pré-requisitos

```bash
# Sobe a aplicação com Docker (PostgreSQL incluído)
./mvnw spring-boot:run

# Ou apenas os testes automatizados
./mvnw verify
```

- App disponível em `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Base: `http://localhost:8080/api/v1`

---

## 2. Fluxo Completo — Criação de OS

```
┌─────────────────────────────────────────────────────────────────────┐
│                    SETUP: dados de suporte                          │
│                                                                     │
│  POST /api/v1/clientes         → obtém clienteDocumento             │
│  POST /api/v1/servicos         → obtém servicoId                    │
│  POST /api/v1/pecas            → obtém pecaId        (opcional)     │
│  POST /api/v1/pecas/estoques   → cria estoque        (opcional)     │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│              POST /api/v1/ordens-servico                            │
│                                                                     │
│  {                                                                  │
│    clienteDocumento, veiculoPlaca,                                  │
│    veiculo (se novo), servicos[], pecas[]                           │
│  }                                                                  │
└──────────────────────────────────┬──────────────────────────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │   CriarOrdemServicoUseCase   │
                    │                             │
                    │  1. Validar ≥1 serviço       │
                    │  2. Buscar cliente (CPF/CNPJ)│
                    │  3. Buscar veículo por placa │
                    │     ├─ Existe → checar dono  │
                    │     └─ Novo   → cadastrar    │
                    │  4. Validar serviços no cat. │
                    │  5. Validar peças no cat.    │
                    │  6. Checar estoque disponível│
                    │  7. Gerar número OS-{ANO}-N  │
                    │  8. new OrdemServico(...)     │
                    │     encerrarComposicao()     │
                    │     → AGUARDANDO_RESP_CLI    │
                    │  9. new Orcamento(...)        │
                    │     enviarParaAprovacao()    │
                    │     → PENDENTE_APROVACAO     │
                    │ 10. Salvar OS + Orçamento    │
                    │     (transação única)        │
                    │ 11. Notificar cliente        │
                    └──────────────┬──────────────┘
                                   │
                                   ▼
                    ┌──────────────────────────────┐
                    │   201 Created                │
                    │                              │
                    │   numero: "OS-2026-00001"    │
                    │   status: AGUARDANDO_RESP_CLI│
                    │   orcamento:                 │
                    │     status: PENDENTE_APROV.  │
                    │     valorTotal: calculado    │
                    └──────────────────────────────┘
```

---

## 3. Cenário Feliz Passo a Passo

### Passo 1 — Criar Cliente

```bash
curl -s -X POST http://localhost:8080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "João Silva",
    "documento": "12345678909"
  }' | jq .
```

**Resposta esperada (201):**
```json
{
  "id": "<UUID>",
  "nome": "João Silva",
  "documento": "123.456.789-09",
  "ativo": true
}
```

Salve o `documento` para o próximo passo.

---

### Passo 2 — Criar Serviço no Catálogo

```bash
curl -s -X POST http://localhost:8080/api/v1/servicos \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Troca de óleo",
    "descricao": "Troca completa de óleo do motor",
    "valor": 100.00
  }' | jq .
```

**Resposta esperada (201):**
```json
{
  "id": "<SERVICO_ID>",
  "nome": "Troca de óleo",
  "valor": 100.00,
  "ativo": true
}
```

Salve o `id` do serviço.

---

### Passo 3 (Opcional) — Criar Peça e Estoque

```bash
# Criar peça
PECA_ID=$(curl -s -X POST http://localhost:8080/api/v1/pecas \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "OLEO-5W30",
    "nome": "Óleo 5W30",
    "valorUnitario": 50.00,
    "estoqueMinimo": 5,
    "unidadeMedida": "L",
    "tipoItem": "INSUMO"
  }' | jq -r '.id')

# Criar estoque com 10 unidades
curl -s -X POST http://localhost:8080/api/v1/pecas/estoques \
  -H "Content-Type: application/json" \
  -d "{
    \"pecaInsumoId\": \"$PECA_ID\",
    \"localizacao\": \"Prateleira A1\",
    \"quantidade\": 10
  }" | jq .
```

---

### Passo 4 — Abrir OS (veículo novo)

```bash
curl -s -X POST http://localhost:8080/api/v1/ordens-servico \
  -H "Content-Type: application/json" \
  -d "{
    \"clienteDocumento\": \"12345678909\",
    \"veiculoPlaca\": \"ABC1D23\",
    \"veiculo\": {
      \"marca\": \"Toyota\",
      \"modelo\": \"Corolla\",
      \"ano\": 2020
    },
    \"servicos\": [
      { \"servicoId\": \"$SERVICO_ID\", \"quantidade\": 1 }
    ],
    \"pecas\": [
      { \"pecaId\": \"$PECA_ID\", \"quantidade\": \"2\" }
    ],
    \"observacoes\": \"Cliente relatou barulho ao frear\"
  }" | jq .
```

**Resposta esperada (201):**
```json
{
  "id": "<OS_ID>",
  "numero": "OS-2026-00001",
  "status": "AGUARDANDO_RESPOSTA_CLIENTE",
  "cliente": {
    "id": "<UUID>",
    "nome": "João Silva",
    "documentoMascarado": "123.456.789-09"
  },
  "veiculo": {
    "placa": "ABC1D23",
    "marca": "Toyota",
    "modelo": "Corolla",
    "ano": 2020
  },
  "orcamento": {
    "id": "<UUID>",
    "valorTotal": 200.00,
    "status": "PENDENTE_APROVACAO"
  },
  "observacoes": "Cliente relatou barulho ao frear"
}
```

**Cálculo do valor:**
- Troca de óleo: R$ 100,00 × 1 = R$ 100,00
- Óleo 5W30: R$ 50,00 × 2 = R$ 100,00
- **Total: R$ 200,00** ✓

---

### Passo 5 — Segunda OS com veículo já cadastrado

```bash
# Criar outro serviço
SERVICO2_ID=$(curl -s -X POST http://localhost:8080/api/v1/servicos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Balanceamento","descricao":"Balanceamento das 4 rodas","valor":60.00}' \
  | jq -r '.id')

# OS sem campo "veiculo" (placa já existe no sistema)
curl -s -X POST http://localhost:8080/api/v1/ordens-servico \
  -H "Content-Type: application/json" \
  -d "{
    \"clienteDocumento\": \"12345678909\",
    \"veiculoPlaca\": \"ABC1D23\",
    \"servicos\": [
      { \"servicoId\": \"$SERVICO2_ID\", \"quantidade\": 1 }
    ]
  }" | jq .
```

**Verificar:** `numero` deve ser `OS-2026-00002` — sequencial correto.

---

## 4. Cenários de Erro

### 4.1 — Cliente não encontrado (404)

```bash
curl -s -X POST http://localhost:8080/api/v1/ordens-servico \
  -H "Content-Type: application/json" \
  -d "{
    \"clienteDocumento\": \"00000000000\",
    \"veiculoPlaca\": \"ABC1D23\",
    \"servicos\": [{ \"servicoId\": \"$SERVICO_ID\", \"quantidade\": 1 }]
  }" | jq .
```

**Esperado:** `404 Not Found`  
**Mensagem:** `"Cliente nao encontrado para o documento informado..."`

---

### 4.2 — Veículo não encontrado sem dados para cadastro (422)

```bash
curl -s -X POST http://localhost:8080/api/v1/ordens-servico \
  -H "Content-Type: application/json" \
  -d "{
    \"clienteDocumento\": \"12345678909\",
    \"veiculoPlaca\": \"ZZZ0Z00\",
    \"servicos\": [{ \"servicoId\": \"$SERVICO_ID\", \"quantidade\": 1 }]
  }" | jq .
```

**Esperado:** `422 Unprocessable Entity`  
**Mensagem:** `"Veiculo nao encontrado pela placa informada. Informe marca, modelo e ano..."`

---

### 4.3 — Veículo de outro cliente (422)

```bash
# Criar segundo cliente
OUTRO_DOC="98765432100"
curl -s -X POST http://localhost:8080/api/v1/clientes \
  -H "Content-Type: application/json" \
  -d "{\"nome\": \"Maria Souza\", \"documento\": \"$OUTRO_DOC\"}" > /dev/null

# Tentar usar a placa do João com a Maria
curl -s -X POST http://localhost:8080/api/v1/ordens-servico \
  -H "Content-Type: application/json" \
  -d "{
    \"clienteDocumento\": \"$OUTRO_DOC\",
    \"veiculoPlaca\": \"ABC1D23\",
    \"servicos\": [{ \"servicoId\": \"$SERVICO_ID\", \"quantidade\": 1 }]
  }" | jq .
```

**Esperado:** `422 Unprocessable Entity`  
**Mensagem:** `"O veiculo informado nao pertence ao cliente..."`

---

### 4.4 — Serviço não encontrado no catálogo (404)

```bash
curl -s -X POST http://localhost:8080/api/v1/ordens-servico \
  -H "Content-Type: application/json" \
  -d '{
    "clienteDocumento": "12345678909",
    "veiculoPlaca": "ABC1D23",
    "servicos": [{ "servicoId": "00000000-0000-0000-0000-000000000000", "quantidade": 1 }]
  }' | jq .
```

**Esperado:** `404 Not Found`

---

### 4.5 — Estoque insuficiente (422)

```bash
# Peça com apenas 1 unidade em estoque
PECA2_ID=$(curl -s -X POST http://localhost:8080/api/v1/pecas \
  -H "Content-Type: application/json" \
  -d '{"sku":"FILTRO-AR","nome":"Filtro de ar","valorUnitario":35.00,"estoqueMinimo":2,"unidadeMedida":"UN","tipoItem":"PECA"}' \
  | jq -r '.id')

curl -s -X POST http://localhost:8080/api/v1/pecas/estoques \
  -H "Content-Type: application/json" \
  -d "{\"pecaInsumoId\":\"$PECA2_ID\",\"localizacao\":\"B2\",\"quantidade\":1}" > /dev/null

# Solicitar 5 unidades com estoque de 1
curl -s -X POST http://localhost:8080/api/v1/ordens-servico \
  -H "Content-Type: application/json" \
  -d "{
    \"clienteDocumento\": \"12345678909\",
    \"veiculoPlaca\": \"ABC1D23\",
    \"servicos\": [{ \"servicoId\": \"$SERVICO_ID\", \"quantidade\": 1 }],
    \"pecas\": [{ \"pecaId\": \"$PECA2_ID\", \"quantidade\": \"5\" }]
  }" | jq .
```

**Esperado:** `422 Unprocessable Entity`  
**Mensagem:** `"Estoque insuficiente para 'Filtro de ar'. Disponivel: 1, solicitado: 5."`

---

### 4.6 — Campos obrigatórios ausentes (400)

```bash
# Sem "servicos" (campo obrigatório)
curl -s -X POST http://localhost:8080/api/v1/ordens-servico \
  -H "Content-Type: application/json" \
  -d '{"clienteDocumento": "12345678909"}' | jq .
```

**Esperado:** `400 Bad Request` — Bean Validation reportando campos faltando.

---

## 4b. Fluxo — Aprovação e Rejeição de Orçamento

Após criar a OS (seção 3), o `id` do orçamento vem no campo `orcamento.id` da resposta.

### Comportamento de reserva de estoque

Ao criar a OS, as peças solicitadas são **imediatamente reservadas** no estoque com movimentação do tipo `RESERVA`. O saldo disponível é decrementado no ato — impedindo que outra OS consuma a mesma quantidade antes da aprovação.

| Evento | Tipo de movimentação | Efeito no estoque |
|--------|---------------------|-------------------|
| OS criada (orçamento gerado) | `RESERVA` | Decrementa quantidade disponível |
| Orçamento aprovado | *(nenhuma — reserva permanece)* | Estoque permanece reservado até execução |
| Orçamento rejeitado | `LIBERACAO` | Devolve quantidade ao saldo disponível |
| Orçamento cancelado | `LIBERACAO` | Devolve quantidade ao saldo disponível |

---

### Aprovar orçamento

```bash
curl -s -X PATCH http://localhost:8080/api/v1/orcamentos/$ORCAMENTO_ID/aprovar | jq .
```

**Resposta esperada (200):**
```json
{
  "id": "<ORCAMENTO_ID>",
  "idOrdemServico": "<OS_ID>",
  "valorTotal": 200.00,
  "status": "APROVADO",
  "tipo": "SERVICO_ORIGINAL",
  "itens": [
    { "descricao": "Troca de óleo", "valor": 100.00 },
    { "descricao": "Óleo 5W30 × 2", "valor": 100.00 }
  ]
}
```

**Efeitos colaterais:**
- Status da OS avança para `AGUARDANDO_EXECUCAO`
- Estoque permanece com a reserva (será debitado na execução do serviço)

---

### Rejeitar orçamento

```bash
curl -s -X PATCH http://localhost:8080/api/v1/orcamentos/$ORCAMENTO_ID/rejeitar | jq .
```

**Resposta esperada (200):**
```json
{
  "id": "<ORCAMENTO_ID>",
  "status": "REJEITADO"
}
```

**Efeitos colaterais:**
- Status da OS retorna para `EM_COMPOSICAO`
- Quantidade de cada peça é devolvida ao estoque com movimentação `LIBERACAO`

---

### Erros esperados

```bash
# 404 — orçamento não encontrado
curl -s -X PATCH http://localhost:8080/api/v1/orcamentos/00000000-0000-0000-0000-000000000000/aprovar | jq .

# 422 — orçamento já aprovado (tentar aprovar novamente)
curl -s -X PATCH http://localhost:8080/api/v1/orcamentos/$ORCAMENTO_ID/aprovar | jq .
curl -s -X PATCH http://localhost:8080/api/v1/orcamentos/$ORCAMENTO_ID/aprovar | jq .
```

---

## 5. Mapa de Status HTTP × Causa

### POST /api/v1/ordens-servico

| Status | Causa |
|--------|-------|
| `201 Created` | OS + Orçamento criados e enviados para aprovação |
| `400 Bad Request` | Campos obrigatórios ausentes ou com formato inválido |
| `404 Not Found` | Cliente não cadastrado ou serviço não existe no catálogo |
| `422 Unprocessable Entity` | Veículo de outro cliente, veículo novo sem dados, estoque insuficiente, serviço inativo |

### PATCH /api/v1/orcamentos/{id}/aprovar e /rejeitar

| Status | Causa |
|--------|-------|
| `200 OK` | Orçamento aprovado ou rejeitado com sucesso |
| `404 Not Found` | Orçamento não encontrado para o ID informado |
| `422 Unprocessable Entity` | Orçamento não está `PENDENTE_APROVACAO`, ou OS em estado inválido |

---

## 6. Verificações de Qualidade

Após criar a OS com sucesso, validar:

| Campo | Valor esperado | Por quê importa |
|-------|---------------|-----------------|
| `status` | `AGUARDANDO_RESPOSTA_CLIENTE` | OS passou por `encerrarComposicao()` |
| `orcamento.status` | `PENDENTE_APROVACAO` | Orçamento passou por `enviarParaAprovacao()` |
| `numero` | `OS-2026-NNNNN` | Sequencial correto por ano, gerado via MAX+1 |
| `orcamento.valorTotal` | soma exata dos itens | Cada item = valorUnitário × quantidade |
| Segunda OS mesma placa | reutiliza veículo, número incrementa | Sem duplicação de veículo |
| Transação | Se qualquer passo falhar, nada é persistido | `@Transactional` no use case |

---

## 7. Conformidade com a Spec (FR checklist)

| Requisito | Descrição | Status |
|-----------|-----------|--------|
| FR-001 | Orcamento vinculado à OS | ✅ |
| FR-002 | id, idOrdemServico, valor, status, itens, tipo | ✅ |
| FR-003 | Pelo menos 1 item no orçamento | ✅ |
| FR-004/005 | Status e tipos corretos | ✅ |
| FR-006/007 | enviarParaAprovacao() CRIADO→PENDENTE | ✅ |
| FR-008/009 | aprovar(OS) PENDENTE→APROVADO + avança OS | ✅ |
| FR-010/011 | rejeitar() PENDENTE→REJEITADO | ✅ |
| FR-014/015 | cancelar(OS) aceita CRIADO, PENDENTE, APROVADO | ✅ |
| FR-016/017 | cancelar(OS) só cancela OS se SERVICO_ORIGINAL | ✅ |
| FR-018/019 | OS só cancelada se `podeSerCancelada()` = true | ✅ |
| FR-020 | aprovar(OS) SERVICO_ORIGINAL → OS.AGUARDANDO_EXECUCAO | ✅ |
| FR-021 | Cancelar orcamento APROVADO não cancela OS em execução | ✅ |

> FR-008 a FR-011 agora estão expostos via `PATCH /api/v1/orcamentos/{id}/aprovar` e `PATCH /api/v1/orcamentos/{id}/rejeitar`.

---

## 8. Testes Automatizados

```bash
# Rodar apenas os testes da feature OS
./mvnw test -pl . -Dtest="CriarOrdemServicoUseCaseTest,AprovarOrcamentoUseCaseTest,RejeitarOrcamentoUseCaseTest,OrdemServicoControllerIT,OrdemServicoTest,OrcamentoTest"

# Rodar tudo com cobertura
./mvnw verify
```

**Cobertura mínima:** 80% (JaCoCo — build falha se não atingir)

| Arquivo de Teste | Tipo | Cenários |
|-----------------|------|---------|
| `OrdemServicoTest` | Unitário | 12 testes — domínio, transições de estado, `podeSerCancelada()` |
| `OrcamentoTest` | Unitário | 18 testes — criação, aprovação, rejeição, cancelamento com coordenação |
| `CriarOrdemServicoUseCaseTest` | Unitário | 11 testes — todos os caminhos do use case com mocks |
| `AprovarOrcamentoUseCaseTest` | Unitário | testes do use case de aprovação com mocks |
| `RejeitarOrcamentoUseCaseTest` | Unitário | testes do use case de rejeição com mocks |
| `OrdemServicoControllerIT` | Integração | 14 testes — criação de OS (9) + aprovação e rejeição de orçamento (5) |
