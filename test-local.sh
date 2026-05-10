#!/bin/bash

# Script para facilitar testes locais das Azure Functions

echo "🚀 Iniciando testes locais das Azure Functions"
echo ""

# Verifica se o Azurite está rodando
echo "📦 Verificando se Azurite está rodando..."
if ! lsof -Pi :10000 -sTCP:LISTEN -t >/dev/null 2>&1 ; then
    echo "⚠️  Azurite não está rodando!"
    echo "   Execute em um terminal separado:"
    echo "   azurite --silent --location ~/azurite"
    echo "   ou"
    echo "   docker run -p 10000:10000 -p 10001:10001 -p 10002:10002 mcr.microsoft.com/azurite"
    echo ""
    read -p "Deseja continuar mesmo assim? (s/n): " continue
    if [[ "$continue" != "s" && "$continue" != "S" ]]; then
        exit 1
    fi
else
    echo "✅ Azurite está rodando"
fi

echo ""
echo "🔨 Compilando o projeto..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Erro ao compilar o projeto"
    exit 1
fi

echo ""
echo "✅ Compilação concluída"
echo ""
echo "📝 Para executar as functions localmente:"
echo "   1. Navegue até: cd src/main/resources"
echo "   2. Execute: func start --java"
echo ""
echo "   Ou execute diretamente:"
echo "   func start --java --script-root target/azure-functions/feedback-api-*/"
echo ""
echo "🧪 Exemplos de teste (após iniciar as functions):"
echo ""
echo "   # Teste 1: Avaliação normal"
echo "   curl -X POST http://localhost:7071/api/avaliacao \\"
echo "     -H \"Content-Type: application/json\" \\"
echo "     -H \"x-functions-key: <FUNCTION_KEY>\" \\"
echo "     -d '{\"descricao\": \"Curso excelente!\", \"nota\": 9}'"
echo ""
echo "   # Teste 2: Avaliação crítica (nota <= 3)"
echo "   curl -X POST http://localhost:7071/api/avaliacao \\"
echo "     -H \"Content-Type: application/json\" \\"
echo "     -H \"x-functions-key: <FUNCTION_KEY>\" \\"
echo "     -d '{\"descricao\": \"Muito ruim\", \"nota\": 2}'"
echo ""
echo "📚 Para mais detalhes, consulte TESTE_LOCAL.md"

