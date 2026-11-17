/**
Problema: Existem $N$ casas dispostas em linha reta ($0$ a $N-1$) e $M$ cores disponíveis ($0$ a $M-1$).
- Pintar a casa $i$ com a cor $c$ tem um custo $C[i][c]$.
- Restrição: Duas casas vizinhas ($i$ e $i+1$) não podem ter a mesma cor ($c_i \neq c_{i+1}$
- Objetivo: Minimizar o custo total para pintar as $N$ casas.

// Retorna o menor valor de uma linha da matriz DP, exeto o pertencente a coluna c.
MIN-COST-LINE-EXEPT(DP, i, c, m):
  minCost = INF
  for k = 0 to m-1:
    if k == c:
      continue
    if DP[i][k] < minCost:
      minCost = DP[i][k]
  return minCost

// Retorna o menor valor de uma linha i de uma matriz com m colunas
MIN-LINE-VALUE(V, i, m):
  minVal = V[i][0]
  for k = 0 to m-1:
    if V[i][k] < minVal:
      minVal = V[i][k]
  return minVal


// Retorna o menor custo possivel para printar as casas dispostas em uma linha
// n: numero de cadas a serem pintadas
// m: número de cores para pintar as casas
// C: matriz de custos para pintura das casas com a cor m
// Exemplo:
//   m = {0(branco), 1 (azul)} (custo 1 e 2)
//   n = 3 (pequena, media, grande) (area 1, 2, 3)
//   C = [1*1 1*2, 2*1 2*2, 3*1 3*2] = [1 2, 2 4, 3 6]
MIN-COST-PAINT-HOUSES-LINE(n, m, C):
  DP = zeros(n, m)
  // caso base: uma unica casa, simplesmente pegar o preço da cor para a casa
  for k = 1 to m:
    DP[0][k] = C[0][k]
  if n > 1:
    // buscar sempre o menor custo acumulado na linha anterior com uma cor diferente da atual
    for k = 1 to n-1: // todas as casas
      for j = 0 to m-1: /// todas as cores
        mindp = MIN-COST-LINE-EXEPT(DP, k-1, j, m)
        newdp = mindp + C[k][j]
        DP[k][j] = newdp
  mincost = MIN-LINE-VALUE(DP, n-1, m-1)
  return mincost

*/

#include <limits.h>
#include <stdio.h>
#include <stdlib.h>
#include <limits.h>

int min_cost_line_except(int *dp_row, int c, int m);
int min_line_value(int *dp_row, int m);
int minCostToN_line(int n, int m, int **C);

int main()
{
    printf("Pintura de $N$ casas dispostas em linha reta usando $M$ cores\n");
    printf("Entrada:\n");
    printf(" - Linha 1: número de casas (N) e número de cores (M)\n");
    printf(" - Próximas N linhas: M inteiros por linha (custos de pintura por cor)\n");
    printf("Saída:\n");
    printf(" - Menor custo total possível\n\n");

    int n, m;
    if (scanf("%d %d", &n, &m) != 2 || n <= 0 || m <= 0)
    {
        return 0;
    }

    int **C = (int **)malloc(n * sizeof(int *));
    if (C == NULL)
    {
        return 1;
    }

    for (int i = 0; i < n; i++)
    {
        C[i] = (int *)malloc(m * sizeof(int));
        if (C[i] == NULL)
        {
            return 1;
        }
        for (int j = 0; j < m; j++)
        {
            if (scanf("%d", &C[i][j]) != 1)
            {
                return 1;
            }
        }
    }

    int result = minCostToN_line(n, m, C);

    printf("Menor gasto de energia/custo possível: %d\n", result);

    for (int i = 0; i < n; i++)
    {
        free(C[i]);
    }
    free(C);

    return 0;
}

// Retorna o menor valor da linha dp_row, ignorando a coluna 'c'
int min_cost_line_except(int *dp_row, int c, int m)
{
    int min_cost = INT_MAX;
    for (int k = 0; k < m; k++)
    {
        if (k == c)
            continue;
        if (dp_row[k] < min_cost)
        {
            min_cost = dp_row[k];
        }
    }
    return min_cost;
}

// Retorna o menor valor absoluto de uma linha da matriz DP
int min_line_value(int *dp_row, int m)
{
    int min_val = dp_row[0];
    for (int k = 1; k < m; k++)
    {
        if (dp_row[k] < min_val)
        {
            min_val = dp_row[k];
        }
    }
    return min_val;
}

// Solução para casas dispostas em linha reta: O(N * M^2) tempo, O(N * M) espaço
int minCostToN_line(int n, int m, int **C)
{
    int **dp = (int **)malloc(n * sizeof(int *));
    for (int i = 0; i < n; i++)
    {
        dp[i] = (int *)malloc(m * sizeof(int));
    }

    // Caso base: casa 0
    for (int j = 0; j < m; j++)
    {
        dp[0][j] = C[0][j];
    }

    // Passo indutivo: casas 1 ate n - 1
    for (int k = 1; k < n; k++)
    {
        for (int j = 0; j < m; j++)
        {
            int min_dp = min_cost_line_except(dp[k - 1], j, m);
            dp[k][j] = C[k][j] + min_dp;
        }
    }

    int result = min_line_value(dp[n - 1], m);

    for (int i = 0; i < n; i++)
    {
        free(dp[i]);
    }
    free(dp);

    return result;
}