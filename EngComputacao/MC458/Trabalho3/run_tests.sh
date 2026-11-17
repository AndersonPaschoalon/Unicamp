#!/usr/bin/env bash

# Configuração da constante do programa C e diretórios
PROGRAM_C="main.c"
EXEC_NAME="./main_app"

# Validação dos argumentos
if [ $# -lt 1 ]; then
    echo "Uso: $0 <arquivo_de_entrada> [arquivo_de_saida_esperada]"
    echo "Exemplo: $0 in/test01.txt out/test01.txt"
    exit 1
fi

INPUT_FILE="$1"
EXPECTED_OUTPUT_FILE="$2"

# 1. Verifica se o arquivo de entrada existe
if [ ! -f "$INPUT_FILE" ]; then
    echo "Erro: Arquivo de entrada '$INPUT_FILE' nao encontrado."
    exit 1
fi

# 2. Compila o programa em C
echo "==> Compilando $PROGRAM_C com gcc..."
gcc -Wall -Wextra -O2 "$PROGRAM_C" -o "$EXEC_NAME" -lm

if [ $? -ne 0 ]; then
    echo "Erro: Falha na compilacao."
    exit 1
fi

echo "==> Compilacao concluida com sucesso!"
echo "----------------------------------------"

# 3. Executa o programa passando a entrada via redirecionamento (stdin)
# Se o segundo argumento for fornecido, redireciona a saída do programa para um arquivo temporário
if [ -n "$EXPECTED_OUTPUT_FILE" ]; then
    
    if [ ! -f "$EXPECTED_OUTPUT_FILE" ]; then
        echo "Aviso: Arquivo de saída esperada '$EXPECTED_OUTPUT_FILE' nao existe."
        echo "Executando apenas a saida no terminal..."
        "$EXEC_NAME" < "$INPUT_FILE"
        exit 0
    fi

    # Criando arquivo temporário para guardar a saída do programa C
    ACTUAL_OUTPUT=$(mktemp)

    echo "==> Executando testes e salvando resultado temporario..."
    "$EXEC_NAME" < "$INPUT_FILE" > "$ACTUAL_OUTPUT"

    echo "==> Exibindo diferencas com o Meld..."
    meld "$ACTUAL_OUTPUT" "$EXPECTED_OUTPUT_FILE"

    # Remove o arquivo temporário após fechar o meld
    rm -f "$ACTUAL_OUTPUT"

else
    # Se nenhum arquivo de saída foi fornecido, simplesmente imprime o resultado na tela
    echo "==> Executando com entrada: $INPUT_FILE"
    echo "--- Saida do Programa ---"
    "$EXEC_NAME" < "$INPUT_FILE"
    echo "-------------------------"
fi