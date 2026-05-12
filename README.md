# Feedback API - TechChallenge FIAP

## Sobre o Projeto
O Tech Challenge é o projeto da fase que englobará os conhecimentos obtidos em todas as disciplinas da fase. 
Esta fase tem como foco o Cloud Computing, Serverless e Deploy de Aplicações em ambiente de nuvem. O projeto proposto envolve a criação de uma plataforma de feedback,
onde os estudantes podem avaliar as aulas e os administradores podem ter acesso a relatórios e análises desses feedbacks.
O objetivo é desenvolver uma aplicação hospedada em um ambiente de nuvem, com funções serverless para automatizar o recebimento de feedbacks, o envio de notificações e a geração de relatórios.

## Tecnologias Utilizadas
- GitHub
- Java 21
- Lombok
- Maven
- Quarkus
- Azure CLI
- Azure Functions
- Azure Storage Tables
- Resend
- Hibernate Validator
- Application Insights

## Funcionalidades

1.  **API de Avaliação** (`POST /api/avaliacao`): Recebe um feedback (JSON com `descricao` e `nota` 0-10).
    - Valida os dados de entrada
    - Calcula automaticamente o nível de urgência baseado na nota
    - Persiste no Azure Storage Tables
2.  **Persistência:** Salva as avaliações e relatórios no Azure Storage Tables.
    - Tabela `avaliacoes`: armazena todos os feedbacks recebidos
    - Tabela `relatorios`: armazena os relatórios semanais gerados
3.  **Notificação Crítica:** Se a `nota` for <= 3, dispara um e-mail de alerta imediatamente para o administrador.
4.  **Relatório Semanal:** Uma função (TimerTrigger) executa semanalmente (toda segunda-feira às 9h), calcula métricas e envia um resumo por e-mail:
    - **Métricas Gerais:** Total de avaliações, média das notas, nota mais alta/baixa
    - **Distribuição por Urgência:** Contagem de avaliações por nível (NORMAL, ALTA, CRITICO)
    - **Análise de Comentários Recorrentes:** Identifica as palavras e frases mais frequentes nos feedbacks
    - **Persistência:** Salva o relatório na tabela `relatorios` para histórico

#### Níveis de Urgência
As avaliações são classificadas automaticamente como:

- **CRITICO:** Nota <= 3 (dispara e-mail imediatamente)
- **ALTA:** Nota entre 4 e 6
- **NORMAL:** Nota >= 7

## Arquitetura

* **Compute:** Azure Functions - Java 21 + Quarkus
* **Injeção de Dependência:** Lombok `@RequiredArgsConstructor`
* **Persistência:** Azure Storage Tables (Tabelas `avaliacoes` e `relatorios`)
* **Análise de Texto:** Processamento de comentários recorrentes
* **Monitoramento:** Application Insights
* **E-mail:** Resend
* **CI/CD:** GitHub Actions

## Diagrama de Arquitetura

![Diagrama de Arquitetura](documentation/diagram.md)

---

## Passo-a-passo do Deploy

### 1. Pré-requisitos Locais
- Azure CLI
- Java 21
- Maven
- Uma conta Resend
- Azure Funcions (para testes locais)

### 2. Criação da Infraestrutura no Azure

Primeiro, clone este repositório. Em seguida, execute o script de criação de infraestrutura.

```bash
# Faça login na sua conta Azure
az login

# Navegue até a pasta de infra
cd infra

# Dê permissão de execução ao script
chmod +x create-resources.sh

# Execute o script
./create-resources.sh
```

O script irá criar:
- Resource Group, Storage Account (com tabelas `avaliacoes` e `relatorios`), Application Insights e um Azure Function App.
- Também define App Settings básicos (`APPLICATIONINSIGHTS_CONNECTION_STRING`, `ADMIN_EMAIL`, `FROM_EMAIL` e um placeholder para `RESEND_API_KEY`).
- As tabelas são criadas automaticamente na primeira execução das funções, caso não existam.

Anote o nome do Function App impresso ao final, pois será usado nos próximos passos.

---

### 3. Configuração no Resend (e-mail)

Para que os e-mails funcionem (alertas críticos e relatório semanal):

1. Crie uma conta no Resend (plano gratuito) e faça login no painel.
2. Verifique um remetente:
   - Opção rápida: Sender Identity único (Single Sender Verification) com o e-mail que você controlará. Esse será o `FROM_EMAIL`.
   - Opção recomendada: Domain Authentication (requer ajustar DNS do seu domínio).
3. Gere uma API Key:
   - Acesse: Settings > API Keys > Create API Key.
   - Permissões: “Restricted Access” com “Mail Send: Full Access”.
   - Copie a chave (você não verá novamente).
4. Guarde:
   - `RESEND_API_KEY`
   - `FROM_EMAIL` (o remetente verificado)
   - `ADMIN_EMAIL` (quem receberá os alertas e relatórios)

> **Importante:** Para que os e-mails sejam efetivamente enviados, o Resend exige que o domínio do remetente (`FROM_EMAIL`) seja validado. Como alternativa para testes rápidos, você pode usar o e-mail padrão `onboarding@resend.dev` como remetente, que já é verificado pela plataforma.

---

### 4. Configurar App Settings no Azure Function App

No Portal Azure:
1. Acesse o recurso do seu Function App > Settings > Configuration.
2. Em Application settings, crie/atualize as chaves abaixo:
   - `ADMIN_EMAIL` = email do administrador que receberá alertas/relatórios.
   - `FROM_EMAIL` = remetente verificado no Resend.
   - `RESEND_API_KEY` = a chave criada no Resend.
   - `APPLICATIONINSIGHTS_CONNECTION_STRING` já deve estar definido pelo script.
   - `AzureWebJobsStorage` já está configurado ao criar o Function App (não altere).
3. Salve e aplique o restart quando solicitado.

Se quiser fazer via CLI:
```bash
az functionapp config appsettings set \
  -g <SEU_RESOURCE_GROUP> \
  -n <SEU_FUNCTION_APP_NAME> \
  --settings ADMIN_EMAIL="seu-admin@exemplo.com" FROM_EMAIL="seu-remetente@exemplo.com" RESEND_API_KEY="SG.xxxxx"
```

Para testes a partir de uma UI Web, você pode liberar CORS (use apenas durante desenvolvimento):
```bash
az functionapp cors add -g <SEU_RESOURCE_GROUP> -n <SEU_FUNCTION_APP_NAME> --allowed-origins "*"
```

---

### 5. Configuração do Git/GitHub e Secrets (CI/CD)

1. Suba este código para um repositório no GitHub (branch `main`):
   - git init, git remote add origin, git add ., git commit -m "init", git push -u origin main.
2. No GitHub, vá em Settings > Secrets and variables > Actions > New repository secret e cadastre:
   - `FUNCTION_APP_NAME` = Nome do Function App criado (ex.: `func-tech-challenge-xxxx`).
   - `AZURE_CREDENTIALS` = Publish Profile do Function App:
     - No Portal Azure: Function App > Overview > Get publish profile > baixe o arquivo `.PublishSettings` e cole o conteúdo inteiro como valor do secret.
   - (Opcional, se preferir injetar via pipeline) `RESEND_API_KEY`, `ADMIN_EMAIL`, `FROM_EMAIL`.

O workflow em `.github/workflows/deploy.yml` já está preparado para:
- Buildar o projeto com Maven/Quarkus para Azure Functions.
- Publicar usando o `publish-profile` armazenado em `AZURE_CREDENTIALS`.
- Usar `FUNCTION_APP_NAME` para direcionar o deploy.

Se optar por enviar variáveis sensíveis via Azure App Settings (recomendado), não é necessário adicioná-las como secrets no GitHub.

---

### 6. Disparar o Deploy

Faça um commit na branch `main` ou acione manualmente um push. O GitHub Actions rodará o job “Deploy Quarkus App to Azure Functions”.

Após a execução, valide no Portal Azure:
- Function App > Functions: a função HTTP deve aparecer (ex.: `httpAvaliacao`).
- Function App > Configuration: app settings presentes.
- Application Insights: logs e traces sendo coletados.

---

### 7. Testes Locais (Opcional)

Para testar a aplicação localmente antes do deploy:

1. **Instalar Azure Functions Core Tools:**
   ```bash
   npm install -g azure-functions-core-tools@4
   ```

2. **Instalar e iniciar Azurite (emulador do Azure Storage):**
   ```bash
   npm install -g azurite
   azurite --silent --location ~/azurite
   ```

3. **Configurar variáveis de ambiente locais:**
   - Edite `src/main/resources/local.settings.json`
   - Configure `RESEND_API_KEY`, `FROM_EMAIL`, `ADMIN_EMAIL`

4. **Executar as funções localmente:**
   ```bash
   mvn clean package
   cd target/azure-functions/feedback-platform-1.0.0
   func start --java
   ```

5. **Testar a API:**
   ```bash
   curl -X POST http://localhost:7071/api/avaliacao \
     -H "Content-Type: application/json" \
     -d '{"descricao": "Teste de feedback", "nota": 5}'
   ```

Para mais detalhes, consulte o arquivo `TESTE_LOCAL.md`.

---

### 8. Testes Rápidos

1. Invocar a API de avaliação (HTTP Trigger):
   - URL típica: `https://<SEU_FUNCTION_APP_NAME>.azurewebsites.net/api/avaliacao`
   - Corpo JSON:
   ```json
   {
     "descricao": "Gostei do atendimento",
     "nota": 3
   }
   ```
   - Esperado: HTTP 201. Se `nota <= 3`, um e-mail é enviado ao `ADMIN_EMAIL`.

2. Relatório semanal (Timer Trigger):
   - O job roda automaticamente pela CRON configurada na função. Você pode executar manualmente (Run) pelo Portal Azure > Functions > sua função de relatório.

---

## Como Contribuir
Contribuições são sempre bem-vindas! Veja como:

1. Fork o projeto
2. Crie sua Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a Branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Contato
- Lucas Bruner - lucasbrunerbruner@gmail.com
- Brenda Bernat - brendalouisebernat@gmail.com

## Licença
Distribuído sob a licença MIT. Veja `LICENSE` para mais informações.