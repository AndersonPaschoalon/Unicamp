#include <stdio.h>
#include <stdlib.h>

typedef struct
{
    int id;
    long long x;
    long long y;
} Point;

//
// Declaração das funções
//

void print_all_points(Point *p, int n);

int main(void)
{
    int n;

    if (scanf("%d", &n) != 1)
    {
        fprintf(stderr, "Erro ao ler o numero de pontos.\n");
        return 1;
    }

    Point *points = (Point *)malloc(n * sizeof(Point));
    if (points == NULL)
    {
        fprintf(stderr, "Erro de alocacao de memoria.\n");
        return 1;
    }

    for (int i = 0; i < n; i++)
    {
        points[i].id = i;
        if (scanf("%lld %lld", &points[i].y, &points[i].x) != 2)
        {
            fprintf(stderr, "Erro ao ler as coordenadas do ponto %d.\n", i + 1);
            free(points);
            return 1;
        }
    }

    // TODO
    printf("Algoritimo implementadio aqui....\n\n");
    print_all_points(points, n);

    free(points);
    return 0;
}

//
// Implementações das Funções
//

void print_all_points(Point *p, int n)
{
    for (int i = 0; i < n; i++)
    {
        printf("[%d] (x, y): (%lld, %lld)\n", p[i].id, p[i].x, p[i].y);
    }
}
