# FordCare Intelligence — Plano de Resposta a Incidentes

## 1. Objetivo

Este documento define o processo de identificação, análise, contenção,
erradicação e recuperação de incidentes de segurança relacionados à
plataforma FordCare Intelligence.

A estratégia utiliza logs de segurança, registros de auditoria,
Spring Boot Actuator, Micrometer, Prometheus e Grafana para fornecer
visibilidade sobre o funcionamento e a segurança da aplicação.

---

## 2. Monitoramento e Detecção

A aplicação disponibiliza métricas por meio do Spring Boot Actuator e
Micrometer.

O Prometheus realiza a coleta periódica dessas métricas e o Grafana é
utilizado para visualização operacional.

O dashboard monitora:

- disponibilidade da API;
- utilização de CPU;
- utilização de memória JVM;
- conexões com o banco de dados;
- quantidade de requisições;
- erros HTTP 4xx e 5xx;
- latência HTTP p95;
- eventos de log WARN e ERROR.

Também são registrados eventos de segurança como:

- LOGIN_SUCCESS;
- LOGIN_FAILED;
- UNAUTHORIZED_ACCESS;
- ACCESS_DENIED;
- RATE_LIMIT_EXCEEDED;
- CUSTOMER_ANONYMIZED.

---

## 3. Alertas

O Prometheus possui regras automáticas para identificação de situações
anormais.

### FordCareApiDown

Se a API permanecer indisponível por pelo menos 1 minuto, o alerta é
classificado como CRITICAL.

### FordCareHighServerErrorRate

Se mais de 5% das requisições apresentarem respostas HTTP 5xx durante
o período configurado, o alerta é classificado como WARNING.

### FordCareHighJvmMemory

Se o consumo de memória JVM ultrapassar 85% do limite configurado
durante o período definido, o alerta é classificado como WARNING.

---

## 4. Processo de Resposta a Incidentes

### 4.1 Detecção

O incidente pode ser identificado através de:

- alertas do Prometheus;
- dashboard do Grafana;
- logs da aplicação;
- registros de auditoria;
- falhas de autenticação;
- aumento de erros HTTP;
- bloqueios por rate limiting;
- indisponibilidade da API.

O evento deve ser registrado para investigação.

### 4.2 Análise

Após a detecção, devem ser analisados:

- horário do evento;
- endpoint afetado;
- usuário relacionado;
- endereço IP registrado;
- código HTTP;
- logs de segurança;
- registros de auditoria;
- métricas anteriores e posteriores ao incidente.

O objetivo é determinar a origem, extensão e impacto do incidente.

### 4.3 Contenção

Dependendo do incidente, podem ser adotadas ações como:

- bloqueio temporário de acessos suspeitos;
- limitação de requisições;
- revogação ou invalidação de credenciais;
- restrição de endpoints;
- isolamento de containers afetados;
- retirada temporária de um serviço comprometido.

A prioridade é impedir que o incidente continue causando impacto.

### 4.4 Erradicação

Após a contenção, a causa do incidente deve ser eliminada.

As ações podem incluir:

- correção de vulnerabilidades;
- atualização de dependências;
- rotação de credenciais;
- remoção de secrets expostos;
- atualização de imagens Docker;
- correção das regras de autorização;
- aplicação de patches de segurança.

As ferramentas SAST, SCA, Secret Scanning e Container Security do
pipeline DevSecOps podem ser executadas novamente para verificar as
correções.

### 4.5 Recuperação

Após a erradicação:

- os serviços são restaurados;
- a API é validada através do health check;
- os testes automatizados são executados;
- o Prometheus confirma a disponibilidade;
- o Grafana é utilizado para acompanhar a estabilização;
- os endpoints críticos são testados;
- a integridade dos dados é verificada.

O ambiente somente deve ser considerado recuperado após a validação
dos controles técnicos.

### 4.6 Lições Aprendidas

Após o incidente, deve ser realizada uma revisão contendo:

- causa raiz;
- impacto observado;
- controles que funcionaram;
- controles que falharam;
- tempo de detecção;
- tempo de recuperação;
- melhorias necessárias;
- novas regras de monitoramento ou segurança.

As melhorias identificadas devem retornar ao ciclo DevSecOps.

---

## 5. Fluxo de Resposta

Detecção
↓
Análise
↓
Contenção
↓
Erradicação
↓
Recuperação
↓
Lições Aprendidas
↓
Melhoria Contínua

---

## 6. Cenários Monitorados

### Tentativas de autenticação inválidas

Evento:

LOGIN_FAILED

Resposta:

Analisar frequência, usuário e origem. O rate limiting reduz tentativas
automatizadas e os eventos permanecem disponíveis para auditoria.

### Acesso sem autenticação

Evento:

UNAUTHORIZED_ACCESS

Resposta:

Verificar endpoint, origem e frequência das tentativas.

### Tentativa de acesso sem permissão

Evento:

ACCESS_DENIED

Resposta:

Analisar usuário, perfil e recurso solicitado para identificar tentativa
indevida de elevação de privilégio ou erro de configuração.

### Excesso de requisições

Evento:

RATE_LIMIT_EXCEEDED

Resposta:

Analisar a origem e verificar possibilidade de automação abusiva ou
tentativa de indisponibilidade.

### Indisponibilidade da API

Alerta:

FordCareApiDown

Resposta:

Verificar container da API, conectividade, PostgreSQL, logs e métricas
antes da restauração.

### Taxa elevada de erros internos

Alerta:

FordCareHighServerErrorRate

Resposta:

Analisar logs, endpoints afetados, banco de dados e alterações recentes.

### Consumo elevado de memória

Alerta:

FordCareHighJvmMemory

Resposta:

Analisar heap JVM, volume de requisições e comportamento da aplicação.

---

## 7. Evidências

As evidências técnicas do processo de monitoramento incluem:

- FordCare API com health check UP;
- target fordcare-api UP no Prometheus;
- métricas coletadas pelo endpoint /actuator/prometheus;
- dashboard FordCare Intelligence — Security & Observability;
- regras de alerta carregadas no Prometheus;
- logs estruturados de eventos de segurança;
- registros persistentes de auditoria.

Essas evidências demonstram a integração entre observabilidade,
monitoramento e resposta a incidentes na arquitetura do FordCare Intelligence.