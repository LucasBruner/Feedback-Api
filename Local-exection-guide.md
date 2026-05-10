# Feedback Platform — Guia de Execução Local

## Visão Geral

Este projeto é uma **Azure Function** construída com **Quarkus 3.27.1** e **Java 21**.  
Existem dois modos de execução local:

| Modo | Comando | Quando usar |
|---|---|---|
| **Quarkus Dev** | `mvn quarkus:dev` | Desenvolvimento e testes unitários com Live Coding |
| **Azure Functions** | `func start` | Simular o comportamento exato do Azure em produção |

---

## Pré-requisitos

| Ferramenta | Versão mínima | Download |
|---|---|---|
| Java (JDK) | 21 | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org |
| Azure Functions Core Tools | 4.x | `npm install -g azure-functions-core-tools@4` |
| Azurite (emulador Azure Storage) | — | `npm install -g azurite` |
| Node.js | 18+ | https://nodejs.org (necessário para os dois itens acima) |

Verifique as instalações:

```bash
java -version
mvn -version
func --version
azurite --version
```

---

## Modo 1 — Quarkus Dev (recomendado para desenvolvimento)

O modo `quarkus:dev` habilita **Live Coding**: qualquer alteração no código é recompilada e aplicada automaticamente, sem reiniciar a aplicação.

> **Atenção:** O modo `quarkus:dev` pode apresentar instabilidade com os triggers do Azure Functions, especialmente para funções que não são acionadas por HTTP (como `TimerTrigger`). Para testes de integração completos e simulação fiel do ambiente de produção, o modo `func start` é o mais recomendado.

### Passo 1 — Subir o Azurite

O Azurite emula o Azure Storage localmente (necessário para o `azure-data-tables`).  
Abra um terminal separado e execute:

```bash
azurite --silent --location C:\azurite --debug C:\azurite\debug.log
```

Aguarde a mensagem:
```
Azurite Blob service is starting at http://127.0.0.1:10000
Azurite Queue service is starting at http://127.0.0.1:10001
Azurite Table service is starting at http://127.0.0.1:10002
```

> **Dica:** mantenha esse terminal aberto durante todo o desenvolvimento.

### Passo 2 — Iniciar o Quarkus Dev

No diretório raiz do projeto (onde está o `pom.xml`):

```bash
mvn quarkus:dev
```

A aplicação estará pronta quando você ver:

```
INFO  [io.quarkus] feedback-api 1.0.0 on JVM (powered by Quarkus 3.27.1) started in Xs.
INFO  [io.quarkus] Profile dev activated. Live Coding activated.
INFO  [io.quarkus] Installed features: [azure-functions, cdi, hibernate-validator]
```

A aplicação fica disponível em: **`http://localhost:8080`**

### Passo 3 — Executar os testes

Com o modo dev ativo, pressione **`r`** no terminal para rodar todos os testes:

```
Press [r] to resume testing
```

Ou execute diretamente em outro terminal:

```bash
mvn test
```

### Controles interativos do modo dev

| Tecla | Ação |
|---|---|
| `r` | Rodar / re-rodar testes |
| `o` | Alternar exibição do output dos testes |
| `e` | Editar argumentos da linha de comando |
| `h` | Exibir todas as opções |
| `q` | Encerrar o Quarkus Dev |

---

## Modo 2 — Azure Functions Core Tools (`func start`)

Use esse modo para simular o ambiente de produção do Azure exatamente como ele se comporta no deploy.

### Passo 1 — Subir o Azurite

Igual ao Modo 1 (veja acima).

### Passo 2 — Build do projeto

```bash
mvn clean package -DskipTests
```

### Passo 3 — Iniciar as Functions

```bash
func start
```

Os endpoints serão exibidos assim que o worker Java inicializar:

```
Functions:
    AvaliacaoHandler: [POST] http://localhost:7171/api/avaliacao
    RelatorioSemanalHandler: [TimerTrigger] Use the [POST] http://localhost:7171/admin/functions/RelatorioSemanalHandler
```

### Alternativa: Executando com `func start --script-root-target`

Uma abordagem mais direta para testar as funções locais, sem a necessidade de executar `mvn package` a cada alteração, é apontar o `func start` diretamente para o diretório de build do Maven. Isso permite um ciclo de desenvolvimento mais rápido.

**Pré-requisito:** É necessário ter compilado o projeto pelo menos uma vez com `mvn compile`.

**Comando:**

```bash
func start --script-root-target target/azure-functions/fiap-feedback-api
```

**Como funciona:**

*   `--script-root-target`: Este parâmetro instrui as `Azure Functions Core Tools` a procurar os arquivos da função (como `function.json` e os JARs) em um diretório específico.
*   `target/azure-functions/fiap-feedback-api`: Este é o diretório onde o Quarkus gera os artefatos necessários para o Azure Functions após a compilação.

> **Importante:** Ao usar `--script-root-target`, o comando `func start` ainda espera encontrar o arquivo `local.settings.json` no diretório atual (a raiz do projeto), e não no diretório de script. Certifique-se de que o `local.settings.json` está presente na raiz do seu projeto ao executar o comando.

Com este método, você só precisa recompilar o código-fonte (`mvn compile`) para que o `func start` utilize a versão mais recente, evitando o processo completo de empacotamento.

---

## Variáveis de Ambiente

As configurações locais ficam no arquivo `local.settings.json` na raiz do projeto.  
**Este arquivo nunca deve ser commitado no Git** (já está no `.gitignore`).

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "RESEND_API_KEY": "<sua-chave-do-resend>",
    "FROM_EMAIL": "onboarding@resend.dev",
    "ADMIN_EMAIL": "<seu-email>",
    "APPLICATIONINSIGHTS_CONNECTION_STRING": "",
    "languageWorkers__java__arguments": "-Xms256m -Xmx512m --add-opens java.base/java.time=ALL-UNNAMED --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED"
  },
  "Host": {
    "LocalHttpPort": 7171,
    "CORS": "*",
    "CORSCredentials": false
  }
}
```

---

## Chamando os Endpoints Localmente

### AvaliacaoHandler — `POST /api/avaliacao`

```bash
curl -X POST http://localhost:8080/api/avaliacao \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": "cliente-123",
    "nota": 2,
    "comentario": "Produto com defeito, entrega atrasada."
  }'
```

### RelatorioSemanalHandler — Trigger manual (modo `func start`)

```bash
curl -X POST http://localhost:7171/admin/functions/RelatorioSemanalHandler \
  -H "Content-Type: application/json" \
  -d '{}'
```