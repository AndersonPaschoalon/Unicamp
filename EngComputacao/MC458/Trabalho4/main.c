/*
// Retorna o menor valor de uma linha da matriz DP, exceto o pertencente à coluna c.
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

// **versão inicial, resolvendo o problema para casas dispostas em linha**
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


// Retorna o menor custo possivel para pintar as casas dispostas em CÍRCULO
// A maior diferença desse algoritimo para o de linha é que adicionamos um loop adicional no qual
// fixamos cada uma das cores como possubilidade para a primeira casa, e ao filtrar a melhor
// opção possivel ao final do loop, deliberadamente ignoramos o caso em que a ultima casa possua
// a mesma cor que a primeira. Essencialmente é um "força bruta" sobre o algorimo anteior das
// casas dispostas em linha
MIN-COST-PAINT-HOUSES-CIRCULAR(n, m, C):
  // Caso base: havendo uma unica casa, apenas retorna o custo da tinta mais barata.
  if n == 1:
    return MIN-LINE-VALUE(C, 0, m)

  globalMinCost = INF
  for firstColor = 0 to m-1:

    DP = matriz_inf(n, m) // cria uma matrix inicializada com INF

    // caso base: fixar a casa 0 com a cor firstColor
    DP[0][firstColor] = C[0][firstColor]

    if n > 1:
      // buscar sempre o menor custo acumulado na linha anterior com uma cor diferente da atual
      for k = 1 to n-1: // todas as casas
        for j = 0 to m-1: // todas as cores
          mindp = MIN-COST-LINE-EXEPT(DP, k-1, j, m)
          if mindp != INF:
            newdp = mindp + C[k][j]
            DP[k][j] = newdp

      mincost = MIN-COST-LINE-EXEPT(DP, n-1, firstColor, m)

    // menor custo global encontrado
    if mincost < globalMinCost:
      globalMinCost = mincost

  if globalMinCost == INF:
    return -1

  return globalMinCost
*/

#include <limits.h>
#include <stdio.h>
#include <stdlib.h>

typedef struct _Matrix
{
    int *M;
    int n;
    int m;
} Matrix;

void create_matrix(Matrix *Mtr, int n, int m, int initial_value);
void reset_matrix(Matrix *Mtr, int initial_value);
void free_matrix(Matrix *Mtr);
int min_cost_line_except(Matrix *DP, int i, int c);
int min_line_value(Matrix *M, int i);
int min_cost_paint_houses_circular(int n, int m, Matrix *C);

int main()
{

    //
    // Ler stdin e preencher n, m e C[][]
    // - n        : numero de casas
    // - m        : numero de cores
    // - C[i][j]  : custo para pintar a casa i com a cor j
    //

    int n = 0;
    int m = 0;
    scanf("%d %d", &n, &m);
    if (n <= 0 || m <= 0)
    {
        printf("Error: Invalid n/m values\n");
        exit(-1);
    }

    Matrix *C = (Matrix *)malloc(sizeof(Matrix));
    create_matrix(C, n, m, 0);
    int val;
    for (int i = 0; i < n; i++)
    {
        int *C_row_i = &C->M[i * m];
        for (int j = 0; j < m; j++)
        {
            scanf("%d", &val);
            C_row_i[j] = val;
            // C->M[i][j] = val;
        }
    }

    int result = min_cost_paint_houses_circular(n, m, C);

    // printf("Menor gasto de energia/custo possível: %d\n", result);
    printf("%d\n", result);

    //
    // Liberar memória alocada de C.
    //
    free_matrix(C);
    C = NULL;

    return 0;
}

void create_matrix(Matrix *Mtr, int n, int m, int initial_value)
{
    if (n <= 0 || m <= 0)
    {
        printf("Error: Invalid n/m values\n");
        exit(-2);
    }

    Mtr->n = n;
    Mtr->m = m;
    Mtr->M = (int *)malloc(n * m * sizeof(int));

    if (Mtr->M == NULL)
    {
        printf("Not enough memory\n");
        exit(-1);
    }

    reset_matrix(Mtr, initial_value);
}

void reset_matrix(Matrix *Mtr, int initial_value)
{
    if (Mtr == NULL || Mtr->M == NULL)
        return;

    int total_elements = Mtr->n * Mtr->m;
    for (int i = 0; i < total_elements; i++)
    {
        Mtr->M[i] = initial_value;
    }
}

void free_matrix(Matrix *Mtr)
{
    if (Mtr == NULL)
        return;

    if (Mtr->M != NULL)
    {
        free(Mtr->M);
        Mtr->M = NULL;
    }

    Mtr->n = 0;
    Mtr->m = 0;
    free(Mtr);
}

// Retorna o menor valor da linha i da matrix de custo acumulado DP, ignorando a coluna  de cor c
// DP: matriz de custos acumulados
// i : linha que será verificada
// c : cor a ser ignorada na linha verificada
// m : quantidade de cores
int min_cost_line_except(Matrix *DP, int i, int c)
{
    int min_cost = INT_MAX;
    int *row = &DP->M[i * DP->m];

    for (int k = 0; k < DP->m; k++)
    {
        if (k == c)
            continue;

        if (row[k] < min_cost)
        {
            min_cost = row[k];
        }
    }
    return min_cost;
}

// Retorna o menor valor de uma linha i da matriz M
// M : matriz
// i : linha a ser avaliada
int min_line_value(Matrix *M, int i)
{
    int *row = &M->M[i * M->m];
    int min_val = row[0];
    for (int k = 1; k < M->m; k++)
    {
        if (row[k] < min_val)
        {
            min_val = row[k];
        }
    }
    return min_val;
}

// Solução para casas dispostas em linha em um circulo
int min_cost_paint_houses_circular(int n, int m, Matrix *C)
{
    // Caso base: havendo uma unica casa, apenas retorna o custo da tinta mais barata.
    if (n == 1)
    {
        return min_line_value(C, 0);
    }

    int globalMinCost = INT_MAX;
    int mindp = INT_MAX;
    int newdp = INT_MAX;
    int mincost = INT_MAX;

    Matrix *DP = (Matrix *)malloc(sizeof(Matrix));
    create_matrix(DP, n, m, INT_MAX);
    int *C_row0 = &C->M[0];
    int *DP_row0 = &DP->M[0];

    for (int firstColor = 0; firstColor < m; firstColor++)
    {
        reset_matrix(DP, INT_MAX);

        // caso base: fixar a casa 0 com a cor firstColor
        DP_row0[firstColor] = C_row0[firstColor];
        if (n > 1)
        {
            // buscar sempre o menor custo acumulado na linha anterior com uma cor diferente da atual
            for (int k = 1; k < n; k++) // todas as casas
            {
                int *C_row_k = &C->M[k * C->m];
                int *DP_row_k = &DP->M[k * DP->m];

                for (int j = 0; j < m; j++) // todas as cores
                {
                    mindp = min_cost_line_except(DP, k - 1, j);
                    if (mindp != INT_MAX)
                    {
                        newdp = mindp + C_row_k[j];
                        DP_row_k[j] = newdp;
                    }
                }
            }
            mincost = min_cost_line_except(DP, n - 1, firstColor);
        }

        // menor custo global encontrado
        if (mincost < globalMinCost)
        {
            globalMinCost = mincost;
        }
    }

    if (globalMinCost == INT_MAX)
    {
        return (-1);
    }

    free_matrix(DP);
    return globalMinCost;
}