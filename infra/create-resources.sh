# Script para criar a infraestrutura mínima no Azure

LOCATION="eastus"
RESOURCE_GROUP="feedback-api-tech-challenge"
STORAGE_NAME="stf4challenge$(openssl rand -hex 4)"
FUNCTION_APP_NAME="func-tech-challenge-$(openssl rand -hex 4)"

ADMIN_EMAIL="rm367812@fiap.com.br"
FROM_EMAIL="onboarding@resend.dev"

echo "Iniciando criação de recursos..."
echo "Grupo de Recursos: $RESOURCE_GROUP"
echo "Storage Account: $STORAGE_NAME"
echo "Function App: $FUNCTION_APP_NAME"
echo "Localização: $LOCATION"

az login

echo "Registrando provedores de recursos necessários..."
az provider register --namespace Microsoft.Storage
az provider register --namespace Microsoft.Insights
az provider register --namespace Microsoft.OperationalInsights
az provider register --namespace Microsoft.Web

echo "Criando Grupo de Recursos..."
az group create --name $RESOURCE_GROUP --location $LOCATION

echo "Criando Storage Account..."
az storage account create \
  --name $STORAGE_NAME \
  --location $LOCATION \
  --resource-group $RESOURCE_GROUP \
  --sku Standard_LRS \
  --allow-blob-public-access false

echo "Obtendo connection string do storage..."
STORAGE_CONNECTION_STRING=$(az storage account show-connection-string --name $STORAGE_NAME --resource-group $RESOURCE_GROUP --query connectionString -o tsv)

echo "Criando Tabela 'avaliacoes'..."
az storage table create --name "avaliacoes" --connection-string "$STORAGE_CONNECTION_STRING"

echo "Criando Tabela 'relatorios'..."
az storage table create --name "relatorios" --connection-string "$STORAGE_CONNECTION_STRING"

echo "Criando Fila 'feedbacks-urgentes'..."
az storage queue create --name "feedbacks-urgentes" --connection-string "$STORAGE_CONNECTION_STRING"

echo "Criando Application Insights..."
az monitor app-insights component create \
  --app $FUNCTION_APP_NAME \
  --location $LOCATION \
  --resource-group $RESOURCE_GROUP \
  --kind "web" \
  --application-type "web"

APP_INSIGHTS_CONNECTION_STRING=$(az monitor app-insights component show --app $FUNCTION_APP_NAME -g $RESOURCE_GROUP --query "connectionString" -o tsv)

echo "Criando Function App..."
az functionapp create \
  --name $FUNCTION_APP_NAME \
  --storage-account $STORAGE_NAME \
  --resource-group $RESOURCE_GROUP \
  --consumption-plan-location $LOCATION \
  --functions-version 4 \
  --runtime "java" \
  --runtime-version "21.0" \
  --os-type "linux"

echo "Configurando Application Insights (APPLICATIONINSIGHTS_CONNECTION_STRING)"
az functionapp config appsettings set \
  --name $FUNCTION_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --settings APPLICATIONINSIGHTS_CONNECTION_STRING="$APP_INSIGHTS_CONNECTION_STRING"

echo "Definindo variáveis de ambiente do Function App (ADMIN_EMAIL, FROM_EMAIL, RESEND_API_KEY)"
az functionapp config appsettings set \
  --name $FUNCTION_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --settings ADMIN_EMAIL="$ADMIN_EMAIL" FROM_EMAIL="$FROM_EMAIL" RESEND_API_KEY="defina_no_portal_ou_gh_secrets"

az functionapp cors add --name $FUNCTION_APP_NAME -g $RESOURCE_GROUP --allowed-origins "*"

echo "--- INFRAESTRUTURA CRIADA COM SUCESSO ---"
echo ""
echo "Guarde estas informações:"
echo "Grupo de Recursos: $RESOURCE_GROUP"
echo "Nome do Function App: $FUNCTION_APP_NAME"
echo "Nome do Storage Account: $STORAGE_NAME"
echo "Application Insights Connection String: $APP_INSIGHTS_CONNECTION_STRING"
echo ""
echo "Execute o script 'destroy-resources.sh' para remover tudo."
