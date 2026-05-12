# Data Model: Atualizacao da Documentacao OpenAPI e README

Esta feature nao cria entidades persistentes nem altera o modelo de dominio. Os modelos abaixo representam artefatos documentais que precisam ser revisados e atualizados.

## Contrato de API

**Representa**: O conjunto completo de operacoes publicas expostas pela aplicacao.

**Campos conceituais**:

- titulo
- versao
- descricao geral
- politicas de seguranca
- tags funcionais
- caminhos e metodos HTTP
- schemas de requisicao e resposta
- respostas de erro padronizadas

**Relacionamentos**:

- Contem muitas operacoes documentadas.
- Referencia muitos schemas de dados.
- E complementado pelo guia do projeto no README.

**Regras de validacao**:

- Deve representar 100% dos endpoints publicos.
- Deve manter tags e nomes consistentes entre dominios.
- Deve declarar seguranca global e excecoes por endpoint quando aplicavel.

## Operacao Documentada

**Representa**: Uma acao consumivel por clientes da API.

**Campos conceituais**:

- metodo HTTP
- caminho
- tag funcional
- resumo e descricao
- parametros de caminho, query ou cabecalho
- corpo de requisicao
- respostas de sucesso
- respostas de erro
- requisitos de autenticacao/autorizacao

**Relacionamentos**:

- Pertence a um contrato de API.
- Pode referenciar schemas de requisicao, resposta e erro.

**Regras de validacao**:

- Metodo e caminho devem corresponder a uma controller publica.
- Parametros obrigatorios e opcionais devem refletir o comportamento exposto.
- Respostas 400, 401, 403, 404, 409 e 422 devem ser documentadas quando aplicaveis.

## Schema de Dados

**Representa**: Estrutura de dados usada em requests e responses.

**Campos conceituais**:

- nome
- tipo
- propriedades
- obrigatoriedade
- exemplos
- restricoes de formato ou tamanho

**Relacionamentos**:

- Pode ser usado por varias operacoes documentadas.
- Pode compor outros schemas.

**Regras de validacao**:

- Campos devem usar nomes iguais aos DTOs expostos.
- Campos obrigatorios devem refletir validacoes observaveis pelo consumidor.
- Exemplos devem ser coerentes com os formatos aceitos.

## Guia do Projeto

**Representa**: Trechos do README que orientam descoberta e uso inicial da API.

**Campos conceituais**:

- link ou caminho da documentacao OpenAPI
- modo de executar a aplicacao localmente
- pre-requisitos essenciais
- dominios funcionais cobertos
- observacoes de autenticacao e seguranca

**Relacionamentos**:

- Complementa o contrato de API.
- Deve apontar para a fonte de contrato detalhado.

**Regras de validacao**:

- Nao deve duplicar a lista completa de endpoints.
- Nao deve contradizer o `openapi.yaml`.
- Deve permitir que uma pessoa nova localize e valide a API em ate 10 minutos.
