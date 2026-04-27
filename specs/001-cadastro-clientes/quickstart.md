# Quickstart: Workshop Service (Cadastro de Clientes)

## Pré-requisitos
- Java 25
- Docker (para executar Testcontainers no build)
- Maven

## Compilando e Executando
Para compilar e rodar os testes utilizando Testcontainers:
```bash
./mvnw clean install
```

Para rodar a aplicação localmente:
```bash
./mvnw spring-boot:run
```

## Exemplo de Uso (API)

**Cadastrar novo cliente**
```bash
curl -X POST http://localhost:8080/api/v1/clientes \
-H "Content-Type: application/json" \
-d '{
  "nome": "João Mecânico",
  "documento": "12345678909",
  "email": "joao@example.com"
}'
```

**Retorno Esperado (201 Created)**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "nome": "João Mecânico",
  "documentoMascarado": "***.456.789-**",
  "email": "joao@example.com",
  "telefone": null,
  "dataCadastro": "2026-04-24T10:00:00Z"
}
```
