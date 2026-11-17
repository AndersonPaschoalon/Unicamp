/**
============================================================================
# Algoritimo de Comparação Guloso COMPARE-CARDS()
============================================================================

Nele aplicamos uma estratégica gulosa para encontrar uma
solução local. Na situação que temos apenas duas cartas
A e B, a melhor estratégia local é simplemente concatenar
os valores de ambas as cartas A e Be avaliar diretamente
quem fica na primeira posição.

// A função retorna TRUE caso a seja a primeira carta, e
// retorna FALSE caso b seja a segunda.
COMPARE-CARDS(a, b)
    s1 = str(a) + str(b)
    s2 = str(b) + str(a)
    r1 = atoi(s1)
    r2 = atoi(s2)

    if  r1 >= r2:
        return TRUE
    else
        return FALSE

============================================================================
# Teste com 3 cartas
============================================================================

Tomemos agora um metodo de ordenação SORT(), que toma como critério
de ordenação a estratégia gulosa COMPARE-CARDS(). Vamos testar com
o exemplo simples uma entraga 3 30 34. Vamos utilizar sem perda de
generalidade o InsertSort por ser mais simples de aplicar manualmente.

A = [a1 a2 a3] = [3 30 34]

(1) [a1 a2] = [3 30]
- a1 vs a2: 330 vs 303 TRUE => ordem correta a1 a2

(2) [a1 a2 a3] = [3 30 34]
- a2 vs a3: 3034 vs 3430 FALSE => a3 a2
    [a1 a3 a2] = [3 34 30]
- a1 vs a3: 334 vs 343 FALSE => a3 a1
    [a3 a1 a2] = [34 3 30]

Número Final: 34330

*/
#define TRUE 1
#define FALSE 0
#define MAX_NUMBER_SIZE 32
#define MAX_NUMBER_CAT_SIZE 64

#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <string.h>

void concat(const char *a, const char *b, char *c, long clength);
int compare_cards(long a, long b);
void merge(long A[], long p, long q, long r);
void merge_sort(long A[], long p, long r);
void print_largest_number(long A[], int n);

int main()
{
    int n;
    if (scanf("%d", &n) != 1 || n <= 0)
    {
        return 0;
    }

    long *A = (long *)malloc(n * sizeof(long));
    if (A == NULL)
    {
        return 1;
    }

    for (int i = 0; i < n; i++)
    {
        scanf("%ld", &A[i]);
    }

    print_largest_number(A, n);

    free(A);
    return 0;
}

void concat(const char *a, const char *b, char *c, long clength)
{
    if (c == NULL || clength <= 0)
        return;

    long len_a = (a != NULL) ? strlen(a) : 0;
    long len_b = (b != NULL) ? strlen(b) : 0;

    long copy_a = (len_a < clength) ? len_a : (clength - 1);
    if (copy_a > 0)
    {
        memcpy(c, a, copy_a);
    }

    long remaining = clength - copy_a;
    long copy_b = (len_b < remaining) ? len_b : (remaining - 1);
    if (copy_b > 0)
    {
        memcpy(c + copy_a, b, copy_b);
    }

    c[copy_a + copy_b] = '\0';
}

int compare_cards(long a, long b)
{
    // Trata as sentinelas para que tenham menor prioridade na ordenação decrescente
    if (a == LONG_MAX)
        return FALSE;
    if (b == LONG_MAX)
        return TRUE;

    char aStr[MAX_NUMBER_SIZE];
    char bStr[MAX_NUMBER_SIZE];
    char s1[MAX_NUMBER_CAT_SIZE];
    char s2[MAX_NUMBER_CAT_SIZE];

    sprintf(aStr, "%ld", a);
    sprintf(bStr, "%ld", b);

    concat(aStr, bStr, s1, MAX_NUMBER_CAT_SIZE);
    concat(bStr, aStr, s2, MAX_NUMBER_CAT_SIZE);

    // Comparação de strings evita estouro de inteiros!
    if (strcmp(s1, s2) >= 0)
    {
        return TRUE;
    }
    else
    {
        return FALSE;
    }
}

void merge(long A[], long p, long q, long r)
{
    long n1 = q - p + 1;
    long n2 = r - q;

    long *L = (long *)malloc(sizeof(long) * (n1 + 1));
    long *R = (long *)malloc(sizeof(long) * (n2 + 1));

    for (long i = 0; i < n1; i++)
    {
        L[i] = A[p + i];
    }
    L[n1] = LONG_MAX; // Sentinela

    for (long j = 0; j < n2; j++)
    {
        R[j] = A[q + 1 + j];
    }
    R[n2] = LONG_MAX; // Sentinela

    long i = 0;
    long j = 0;
    for (long k = p; k <= r; k++)
    {
        if (compare_cards(L[i], R[j]) == TRUE)
        {
            A[k] = L[i];
            i++;
        }
        else
        {
            A[k] = R[j];
            j++;
        }
    }

    free(L);
    free(R);
}

void merge_sort(long A[], long p, long r)
{
    if (p < r)
    {
        long q = p + (r - p) / 2;
        merge_sort(A, p, q);
        merge_sort(A, q + 1, r);
        merge(A, p, q, r);
    }
}

void print_largest_number(long A[], int n)
{
    merge_sort(A, 0, n - 1);
    for (int i = 0; i < n; i++)
    {
        printf("%ld", A[i]);
    }
    printf("\n");
}