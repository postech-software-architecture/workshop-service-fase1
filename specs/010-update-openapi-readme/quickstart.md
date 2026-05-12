# Quickstart: Atualizacao da Documentacao OpenAPI e README

## 1. Conferir branch e arquivos

```powershell
git status --short --branch
```

Arquivos principais da feature:

```text
src/main/java/com/postech/workshop_service/api/controllers/openapi.yaml
README.md
src/main/java/com/postech/workshop_service/api/controllers/*.java
src/test/java/com/postech/workshop_service/api/controllers/*IT.java
```

## 2. Levantar inventario de endpoints

```powershell
rg "@(Get|Post|Put|Patch|Delete)Mapping|@RequestMapping" src/main/java/com/postech/workshop_service/api/controllers -n
```

Compare a saida com `contracts/openapi-documentation-contract.md` e com o arquivo `openapi.yaml`.

## 3. Revisar contrato OpenAPI

Para cada endpoint publico:

- confirmar metodo e caminho
- confirmar tag funcional
- confirmar parametros e request body
- confirmar responses de sucesso e erro
- confirmar schemas e exemplos
- conferir autenticacao/autorizacao quando aplicavel

## 4. Revisar README

Validar se o README permite:

- saber como executar a aplicacao
- localizar o Swagger UI local
- localizar o arquivo `openapi.yaml`
- entender quais dominios funcionais a API cobre

## 5. Validar localmente

Compilacao e testes:

```powershell
mvn test
```

Execucao local:

```powershell
$env:JWT_SECRET="defina-um-segredo-com-pelo-menos-32-caracteres"
mvn spring-boot:run
```

Apos subir a aplicacao, acessar:

```text
http://localhost:8080/swagger-ui/index.html
```

## 6. Criterios de pronto

- Todos os endpoints publicos das controllers estao cobertos no OpenAPI.
- Padroes de erro e validacao estao coerentes com o comportamento exposto.
- README aponta para a documentacao sem contradizer o contrato.
- Validacao local foi executada ou a limitacao foi registrada.
