#!/usr/bin/env bash

# Cria as pastas de entrada e saída se não existirem
mkdir -p in out

echo "==> Gerando arquivos de teste..."

# Teste 01: Exemplo 1 do Enunciado (N=4, M=3)
cat << 'EOF' > in/test01.txt
4 3
1 5 3
4 1 2
2 8 1
9 3 5
EOF
echo "6" > out/test01.txt

# Teste 02: Exemplo 2 do Enunciado (N=3, M=2 -> Impossível)
cat << 'EOF' > in/test02.txt
3 2
1 2
2 1
1 2
EOF
echo "-1" > out/test02.txt

# Teste 03: 3 casas e 3 cores (Exemplo Pequeno Válido)
cat << 'EOF' > in/test03.txt
3 3
1 2 3
2 4 6
3 6 8
EOF
echo "10" > out/test03.txt

# Teste 04: Edge case - Apenas 1 casa (N=1, M=2)
cat << 'EOF' > in/test04.txt
1 2
10 5
EOF
echo "5" > out/test04.txt

# Teste 05: Edge case - 2 casas e 2 cores (N=2, M=2)
cat << 'EOF' > in/test05.txt
2 2
3 7
8 2
EOF
echo "5" > out/test05.txt

# Teste 06: Edge case - 1 única cor com N > 1 (N=3, M=1 -> Impossível)
cat << 'EOF' > in/test06.txt
3 1
5
5
5
EOF
echo "-1" > out/test06.txt

# Teste 07: Custos iguais para todas as cores (N=4, M=3)
cat << 'EOF' > in/test07.txt
4 3
10 10 10
10 10 10
10 10 10
10 10 10
EOF
echo "40" > out/test07.txt

echo "==> Todos os 7 arquivos de teste foram criados em in/ e out/ com sucesso!"

