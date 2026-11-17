/*
O Pulo do Sapo (DP 1D): Um sapo precisa atravessar um rio saltando sobre $N$ pedras dispostas em linha reta
($1$ a $N$).
A pedra $i$ tem altura $h_i$.
O custo de energia ao saltar da pedra $i$ para a pedra $j$ é $|h_i - h_j|$.
Ele parte da pedra $1$ com custo 0 e pode saltar 1 ou 2 pedras à frente (da pedra $i$ para $i+1$ ou $i+2$)13.

Objetivo: Atingir a pedra $N$ com o menor custo total acumulado.
*/

/*
# Algoritimo recursivo ingênuo

MIN-COST-RECURSIVO(H, i)
    if i == 0 então
        return 0
    if i == 1 então
    return abs(H[1] - H[0])
    op1 = MIN-COST-RECURSIVO(H, i - 1) + abs(H[i] - H[i - 1])
    op2 = MIN-COST-RECURSIVO(H, i - 2) + abs(H[i] - H[i - 2])
    return min(op1, op2)


# Algoritimo - Programação dinâmica

EQUACAO-TRANSICAO(i, DP, H):
    a = DP[i - 1] + abs(H[i] - H[i - 1])
    b = DP[i - 2] + abs(H[i] - H[i - 2])
    return min(a, b)

MIN-COST-TO-N(H, n)
    if n == 1:
        return 0
    DP = zeros(n)
    DP[0] = 0
    DP[1] = abs(H[1] - H[0])
    for i = 2 to n-1:
        DP[i] = EQUACAO-TRANSICAO(i, DP, H)
    return DP[n - 1]

*/

#include <stdio.h>
#include <stdlib.h>
#include <time.h>

void stop_clock(clock_t start);
void hline();
int min(int a, int b);
int equacao_transicao(int i, int dp[], int h[]);
void zeros(int v[], int len);
int minCostToN_dinamic(int n, int h[]);
int minCostToN_naive(int n, int h[]);
int minCostToN_naive_rec(int idx, int h[]);

int main()
{

    printf("Um sapo precisa atravessar um rio saltando sobre $N$ pedras dispostas em linha reta ($1$ a $N$)\n");
    printf("Entrada:\n");
    printf(" - Linha 1: número de pedras\n");
    printf(" - Linha 2: altura das pedras\n");
    printf("Saída:\n");
    printf(" - Menor gasto de energia possível\n");

    int n;
    if (scanf("%d", &n) != 1 || n <= 0)
    {
        return 0;
    }

    int *h = (int *)malloc(n * sizeof(int));
    if (h == NULL)
    {
        return 1;
    }

    for (int i = 0; i < n; i++)
    {
        scanf("%d", &h[i]);
    }

    hline();
    clock_t start_naive = clock();
    printf("# Solução: Recursiva ingenua:\n");
    int result = minCostToN_naive(n, h);
    stop_clock(start_naive);
    printf("result: %d\n", result);

    hline();
    clock_t start_dp = clock();
    printf("# Solução: Programação dinâmica\n");
    result = minCostToN_dinamic(n, h);
    stop_clock(start_dp);

    printf("result: %d\n", result);

    free(h);
    return 0;
}

void stop_clock(clock_t start)
{
    clock_t end_dp = clock();
    double total = (double)(end_dp - start) / CLOCKS_PER_SEC;
    printf("Tempo: %.6f segundos\n", total);
}
void hline()
{
    printf("###############################################################################\n");
}

int min(int a, int b)
{
    if (a < b)
    {
        return a;
    }
    else
    {
        return b;
    }
}

int equacao_transicao(int i, int dp[], int h[])
{
    int a = dp[i - 1] + abs(h[i] - h[i - 1]);
    int b = dp[i - 2] + abs(h[i] - h[i - 2]);
    return min(a, b);
}

void zeros(int v[], int len)
{
    for (int i = 0; i < len; i++)
    {
        v[i] = 0;
    }
}

int minCostToN_dinamic(int n, int h[])
{
    if (n == 1)
    {
        return 0;
    }
    int *dp = (int *)malloc(sizeof(int) * n);
    zeros(dp, n);
    dp[1] = abs(h[1] - h[0]);
    for (int i = 2; i < n; i++)
    {
        dp[i] = equacao_transicao(i, dp, h);
    }
    int result = dp[n - 1];
    free(dp);
    dp = NULL;
    return result;
}

int minCostToN_naive_rec(int idx, int h[])
{
    if (idx == 0)
        return 0;
    if (idx == 1)
        return abs(h[1] - h[0]);

    int op1 = minCostToN_naive_rec(idx - 1, h) + abs(h[idx] - h[idx - 1]);
    int op2 = minCostToN_naive_rec(idx - 2, h) + abs(h[idx] - h[idx - 2]);
    return min(op1, op2);
}

int minCostToN_naive(int n, int h[])
{
    return minCostToN_naive_rec(n - 1, h);
}
