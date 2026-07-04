# Risk Engine API

API desenvolvida em **Java** com **Micronaut** para processamento de eventos de risco.

A aplicação recebe eventos relacionados a clientes, calcula uma pontuação de risco com base em regras de negócio e retorna uma decisão automática, como permitir, monitorar, revisar ou bloquear a operação.

## Visão geral

O projeto simula uma engine de análise de risco para eventos como:

* login suspeito;
* alteração de dispositivo;
* atualização de risco da conta;
* transações com valores altos;
* eventos vindos de canais específicos, como mobile;
* análise de risco baseada em metadados, como nível de risco do IP.

Cada evento processado gera um resultado contendo:

* identificador do evento;
* identificador do cliente;
* tipo do evento;
* score de risco;
* decisão tomada;
* motivos que influenciaram a análise;
* data e hora do processamento.

## Tecnologias utilizadas

* Java
* Micronaut
* Jakarta Validation
* Micronaut Serialization
* AWS SDK for Java
* Amazon DynamoDB
* ConcurrentHashMap para armazenamento em memória
* Gradle

## Como funciona

O fluxo principal da aplicação é:

1. A API recebe um evento de risco via HTTP.
2. O evento é transformado em um objeto de domínio.
3. O caso de uso `ProcessRiskEventUseCase` analisa o evento.
4. A aplicação calcula um score de risco.
5. Uma decisão é tomada com base no score.
6. O resultado é salvo no repositório configurado.
7. A API retorna o resultado do processamento.

## Regras de risco

A pontuação de risco é calculada a partir de regras simples de negócio.

| Condição                           | Pontuação |
| ---------------------------------- | --------: |
| Valor maior ou igual a 5000.00     |       +50 |
| Valor maior ou igual a 1000.00     |       +30 |
| Evento de alteração de dispositivo |       +25 |
| Login suspeito                     |       +35 |
| Atualização de risco da conta      |       +20 |
| Canal mobile                       |       +10 |
| IP com risco médio                 |       +15 |
| IP com risco alto                  |       +30 |

O score máximo é limitado a **100 pontos**.

## Decisões possíveis

A decisão final é baseada no score calculado:

|      Score | Decisão               |
| ---------: | --------------------- |
| 85 ou mais | BLOCK                 |
|  60 até 84 | REVIEW                |
|  35 até 59 | ALLOW_WITH_MONITORING |
|   0 até 34 | ALLOW                 |

## Endpoints

### Criar evento de risco

```http
POST /risk-events
```

Exemplo de request:

```json
{
  "customerId": "customer-123",
  "eventType": "LOGIN_SUSPICIOUS",
  "amount": 1500.00,
  "deviceId": "device-abc",
  "channel": "mobile",
  "metadata": {
    "ipRisk": "HIGH"
  }
}
```

Exemplo de response:

```json
{
  "eventId": "generated-event-id",
  "customerId": "customer-123",
  "eventType": "LOGIN_SUSPICIOUS",
  "riskScore": 100,
  "decision": "BLOCK",
  "reasons": [
    "HIGH_AMOUNT",
    "SUSPICIOUS_LOGIN",
    "MOBILE_CHANNEL",
    "HIGH_IP_RISK"
  ],
  "processedAt": "2026-07-04T22:00:00Z"
}
```

### Buscar evento por ID

```http
GET /risk-events/{eventId}
```

Retorna o resultado de risco de um evento específico.

### Buscar eventos por cliente

```http
GET /risk-events/customer/{customerId}
```

Retorna todos os eventos processados para um determinado cliente, ordenados do mais recente para o mais antigo.

### Health check

```http
GET /risk-events/health
```

Response:

```txt
OK
```

## Persistência

A aplicação possui duas implementações de repositório:

### InMemoryRiskEventRepository

Implementação em memória utilizando `ConcurrentHashMap`.

É útil para testes locais, desenvolvimento e execução simples sem dependência externa.

### DynamoRiskEventRepository

Implementação utilizando **Amazon DynamoDB**.

Ela permite persistir os resultados dos eventos em uma tabela DynamoDB e consultar os dados por:

* `eventId`;
* `customerId`.

A consulta por cliente utiliza o índice:

```txt
customerId-processedAt-index
```

## Estrutura do projeto

```txt
com.riskengine
├── api
│   ├── RiskEventController
│   └── DynamoRiskEventRepository
├── application
│   ├── ProcessRiskEventUseCase
│   └── GetRiskEventUseCase
├── domain
│   ├── model
│   │   ├── RiskEvent
│   │   └── RiskResult
│   ├── repository
│   │   └── RiskEventRepository
│   └── enums
├── infra
│   └── InMemoryRiskEventRepository
├── metrics
│   └── StartupMetricsLogger
└── Application
```

## Métricas de inicialização

A aplicação registra métricas básicas no startup, incluindo:

* tempo de inicialização;
* memória usada;
* memória total;
* memória máxima;
* nome da JVM;
* versão do Java.

Exemplo de log:

```txt
startup_metrics startup_ms=... used_memory_mb=... total_memory_mb=... max_memory_mb=... vm_name=... java_version=...
```

## Resultado do benchmark

Foi executado um benchmark simples com **50 requisições** para observar o tempo de resposta da API.

```txt
Média de duração: 54,54 ms
Menor duração: 20 ms
Maior duração: 176 ms
Total de requests: 50
```

## Amostra dos tempos coletados

```txt
request=1  duration_ms=115
request=2  duration_ms=36
request=3  duration_ms=20
request=4  duration_ms=74
request=5  duration_ms=76
...
request=50 duration_ms=30
```

## Interpretação do benchmark

Com uma média de aproximadamente **55 ms por request**, a aplicação apresentou um tempo de resposta baixo para o cenário testado.

Alguns picos acima de **100 ms** foram observados:

```txt
request=6  duration_ms=176
request=27 duration_ms=151
request=34 duration_ms=127
request=46 duration_ms=122
```

Esses picos podem estar relacionados a fatores como aquecimento da JVM, latência de I/O, comunicação com banco de dados, garbage collector ou variações normais do ambiente de execução.

## Como calcular a média

```txt
média = soma_dos_tempos / quantidade_de_requests
média = 2727 / 50
média = 54,54 ms
```

## Possíveis melhorias

Algumas melhorias futuras para o projeto:

* adicionar testes unitários para as regras de risco;
* adicionar testes de integração para os endpoints;
* incluir percentis no benchmark, como p95 e p99;
* separar warm-up das medições reais;
* exportar resultados para CSV ou JSON;
* adicionar paginação na busca por cliente;
* melhorar a modelagem dos motivos de risco no DynamoDB;
* adicionar autenticação nos endpoints;
* adicionar observabilidade com métricas, tracing e logs estruturados;
* criar infraestrutura local com Docker Compose e DynamoDB Local.

## Objetivo do projeto

Este projeto demonstra a construção de uma API de análise de risco com separação entre camadas de domínio, aplicação, infraestrutura e interface HTTP.

A ideia principal é mostrar uma arquitetura simples, limpa e extensível para processamento de eventos, cálculo de score e persistência dos resultados.
