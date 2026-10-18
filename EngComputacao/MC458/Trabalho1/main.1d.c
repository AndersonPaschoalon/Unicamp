#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <math.h>
//
// INPUT
//

#define DISTANCE_TYPE_1D 1

typedef long long lint;

//  Representa um ponto no plano cartesiano de uma aeronave
typedef struct _point
{
    int id;
    lint x;
    lint y;
} Point;

typedef struct _pair_element
{
    int i; // Aeronave 1
    int j; // Aeronave 2
} PairElement;

//
// OUTPUT
//

//
// IMPLEMENTATION
//

// Lista ligada de pares de índices de pontos
typedef struct _node_pair
{
    int i; // Aeronave 1
    int j; // Aeronave 2
    struct _node_pair *next;
} Pair;

// Retorno da função recursiva de calculo da menor distância entre pontos
typedef struct _dist_result
{
    lint min_dist; // Distância mínima
    Pair *head;    // inicio da lista ligada
} DistResult;

typedef struct _final_result
{
    lint min_dist;      // Distância mínima
    PairElement *pairs; // array de pares de pontos com a menor distância
    int num_pairs;      // número de pares de pontos com a menor distância
} FinalResult;

//
// HELPERS
//

lint calc_1d_dist(Point *a, Point *b);
lint calc_2d_dist_square(Point *a, Point *b);
// double calc_2d_dist(Point *a, Point *b);
lint min(lint a, lint b);
lint long_abs(lint a);
lint square(lint a);

void print_all_points(Point *Points, int n);
void merge_x_points(Point Points[], int l, int m, int r);
void merge_sort_x_points(Point Points[], int l, int r);

Pair *add_pair_to_list(Pair *PairsHead, int i, int j);
Pair *merge_lists_of_pairs(Pair *PairsHead1, Pair *PairsHead2);
void free_list_of_pairs(Pair *PairsHead);

void merge_sort_pairs_elements(PairElement *PairArray, int l, int r);
void merge_pairs_elements(PairElement *PairArray, int l, int m, int r);
int compare_pair_elements(PairElement *a, PairElement *b);
PairElement *convert_list_to_array(Pair *PairListHead, int num_pairs);
int count_pairs_nodes_on_ll(Pair *PairsHead);

DistResult min_points_distance_for_small_array(Point Points[], int array_lenght);
DistResult min_distance_x_rec(Point Points[], int array_lenght);
FinalResult min_distance_x(Point Points[], int array_lenght);

int main(void)
{
    int debug = 1;
    int n;

    if (scanf("%d", &n) != 1)
    {
        fprintf(stderr, "Erro ao ler o numero de pontos.\n");
        return 1;
    }

    Point *Points = (Point *)malloc(n * sizeof(Point));
    if (Points == NULL)
    {
        fprintf(stderr, "Erro de alocacao de memoria.\n");
        return 1;
    }

    for (int i = 0; i < n; i++)
    {
        Points[i].id = i;
        if (scanf("%lld %lld", &Points[i].x, &Points[i].y) != 2)
        {
            fprintf(stderr, "Erro ao ler as coordenadas do ponto %d.\n", i + 1);
            free(Points);
            return 1;
        }
    }
    FinalResult res = min_distance_x(Points, n);
    if (debug == 1)
    {
        printf("--- RESULTADO 1D ---\n");
        printf("Menor distancia encontrada: %lld\n", res.min_dist);
        printf("Pares de aeronaves com a menor distancia:\n");
        for (int i = 0; i < res.num_pairs; i++)
        {
            printf("  -> Par: Aeronave %d e Aeronave %d\n", res.pairs[i].i, res.pairs[i].j);
        }
    }
    else
    {
        for (int i = 0; i < res.num_pairs; i++)
        {
            printf("(%d,%d)\n", res.pairs[i].i, res.pairs[i].j);
        }
    }
    free(res.pairs);

    return 0;
}

//
// Auxiliary functions
//

lint calc_1d_dist(Point *a, Point *b)
{
    return long_abs(a->x - b->x);
}

lint calc_2d_dist_square(Point *a, Point *b)
{
    lint delta_x = a->x - b->x;
    lint delta_y = a->y - b->y;
    return square(delta_x) + square(delta_y);
}

// double calc_2d_dist(Point *a, Point *b)
//{
//     return sqrt(calc_2d_dist_square(a, b));
// }

lint min(lint a, lint b)
{
    return (a < b) ? a : b;
}

lint long_abs(lint a)
{
    return (a < 0) ? -a : a;
}

lint square(lint a)
{
    return a * a;
}

void print_all_points(Point *Points, int n)
{
    for (int i = 0; i < n; i++)
    {
        printf("[%d] (x, y): (%lld, %lld)\n", Points[i].id, Points[i].x, Points[i].y);
    }
}

void merge_x_points(Point Points[], int l, int m, int r)
{
    int i;
    int j;
    int k;
    int lsize = m - l + 1; // tamanho do subarray esquerdo
    int rsize = r - m;     // tamanho do subarray direito

    // Temp subarrays
    Point *Left = (Point *)malloc(sizeof(Point) * lsize);
    Point *Right = (Point *)malloc(sizeof(Point) * rsize);

    for (int i = 0; i < lsize; i++)
    {
        Left[i] = Points[l + i];
    }
    int r0 = m + 1; // first element of the right subarray
    for (int j = 0; j < rsize; j++)
    {
        Right[j] = Points[r0 + j];
    }

    // merge two ordered arrays Right and Left back and ordered
    // into the main array points
    i = 0;
    j = 0;
    k = l;
    while (i < lsize && j < rsize)
    {
        if (Left[i].x <= Right[j].x)
        {
            Points[k] = Left[i];
            i++;
        }
        else
        {
            Points[k] = Right[j];
            j++;
        }
        k++; // position of the main array currently being filled
    }
    // Copy all remaining elements of Left, if any
    while (i < lsize)
    {
        Points[k] = Left[i];
        i++;
        k++;
    }
    // Copy all remaining elements of Right, if any
    while (j < rsize)
    {
        Points[k] = Right[j];
        j++;
        k++;
    }

    free(Left);
    free(Right);
}

void merge_sort_x_points(Point Points[], int l, int r)
{
    if (l >= r)
    {
        return;
    }
    // calc the mid position
    int offset = (r - l) / 2;
    int mid = l + offset;
    merge_sort_x_points(Points, l, mid);
    merge_sort_x_points(Points, mid + 1, r);
    merge_x_points(Points, l, mid, r);
}

// Adiciona um par de pontos na lista.
// Como dois pontos em qualquer ordem formam o mesmo seguimento de reta, mantemos
// o menos significativo primeiro por convenção.
// Ou seja, os pares (Pairs) (1, 2), e (2, 1), definem exatamete o mesmo conjunto de pontos.
// Não confundir Pair com Point.
Pair *add_pair_to_list(Pair *PairsHead, int i, int j)
{
    Pair *new_node = (Pair *)malloc(sizeof(Pair));
    if (new_node == NULL)
    {
        fprintf(stderr, "Erro de alocacao de memoria.\n");
        exit(1);
    }

    if (i < j)
    {
        new_node->i = i;
        new_node->j = j;
    }
    else
    {
        new_node->j = i;
        new_node->i = j;
    }
    // head não alocado, retorna o novo nó como head da lista
    if (PairsHead == NULL)
    {
        new_node->next = NULL;
        return new_node;
    }
    // novo no passa a ser o head da lista, e aponta para o antigo head
    // complexidade O(1)
    new_node->next = PairsHead;
    return new_node;
}

// Combina duas listas ligadas de pares
Pair *merge_lists_of_pairs(Pair *PairsHead1, Pair *PairsHead2)
{
    if (PairsHead1 == NULL)
        return PairsHead2;
    if (PairsHead2 == NULL)
        return PairsHead1;

    Pair *curr = PairsHead1;
    while (curr->next != NULL)
    {
        curr = curr->next;
    }
    // curr-> is null at this moment. It should point to the head of the second list, PairsHead2
    curr->next = PairsHead2;
    // head of the first list becomes the head of the combined list
    return PairsHead1;
}

// Libera uma lista de pares de pontos.
void free_list_of_pairs(Pair *PairsHead)
{
    Pair *curr = PairsHead;
    Pair *temp = NULL;
    while (curr != NULL)
    {
        temp = curr;
        curr = curr->next;
        free(temp);
    }
    temp = NULL;
    curr = NULL;
}

int count_pairs_nodes_on_ll(Pair *PairsHead)
{
    int total_nodes = 0;
    Pair *current = PairsHead;
    while (current != NULL)
    {
        total_nodes++;
        current = current->next;
    }
    return total_nodes;
}

PairElement *convert_list_to_array(Pair *PairListHead, int num_pairs)
{
    if (PairListHead == NULL || num_pairs <= 0)
    {
        return NULL;
    }

    PairElement *PairArray = (PairElement *)malloc(sizeof(PairElement) * num_pairs);
    if (PairArray == NULL)
    {
        fprintf(stderr, "Erro de alocacao de memoria para PairArray.\n");
        exit(1);
    }

    Pair *current = PairListHead;
    int index = 0;

    while (current != NULL && index < num_pairs)
    {
        // Garante a convencao  que i < j para cada par
        if (current->i < current->j)
        {
            PairArray[index].i = current->i;
            PairArray[index].j = current->j;
        }
        else
        {
            PairArray[index].i = current->j;
            PairArray[index].j = current->i;
        }

        current = current->next;
        index++;
    }

    return PairArray;
}

// Funcao de comparacao lexicografica entre dois PairElement
// Retorna < 0 se A < B, 0 se A == B, e > 0 se A > B
int compare_pair_elements(PairElement *a, PairElement *b)
{
    if (a->i != b->i)
    {
        return a->i - b->i;
    }
    return a->j - b->j;
}

void merge_pairs_elements(PairElement *PairArray, int l, int m, int r)
{
    int n1 = m - l + 1;
    int n2 = r - m;

    PairElement *LeftArray = (PairElement *)malloc(sizeof(PairElement) * n1);
    PairElement *RightArray = (PairElement *)malloc(sizeof(PairElement) * n2);

    if (LeftArray == NULL || RightArray == NULL)
    {
        fprintf(stderr, "Erro de alocacao de memoria no merge_pairs.\n");
        exit(1);
    }

    for (int i = 0; i < n1; i++)
    {
        LeftArray[i] = PairArray[l + i];
    }
    for (int j = 0; j < n2; j++)
    {
        RightArray[j] = PairArray[m + 1 + j];
    }

    int i = 0;
    int j = 0;
    int k = l;

    while (i < n1 && j < n2)
    {
        if (compare_pair_elements(&(LeftArray[i]), &(RightArray[j])) <= 0)
        {
            PairArray[k] = LeftArray[i];
            i++;
        }
        else
        {
            PairArray[k] = RightArray[j];
            j++;
        }
        k++;
    }

    while (i < n1)
    {
        PairArray[k] = LeftArray[i];
        i++;
        k++;
    }

    while (j < n2)
    {
        PairArray[k] = RightArray[j];
        j++;
        k++;
    }

    free(LeftArray);
    free(RightArray);
}

void merge_sort_pairs_elements(PairElement *PairArray, int l, int r)
{
    if (l < r)
    {
        int m = l + (r - l) / 2;

        merge_sort_pairs_elements(PairArray, l, m);
        merge_sort_pairs_elements(PairArray, m + 1, r);

        merge_pairs_elements(PairArray, l, m, r);
    }
}
// - Transforma a lista ligada de pares em um array de pares
// - Ordena os pares lexicograficamente usando o merge sort
// - Elimina pares duplicados do array de pares
// - Formata o resultado final em uma estrutura FinalResult
FinalResult dist_result_to_final_result(DistResult *dist_result)
{
    FinalResult Result;
    Result.min_dist = dist_result->min_dist;
    Result.pairs = NULL;
    Result.num_pairs = 0;

    int total_nodes = count_pairs_nodes_on_ll(dist_result->head);
    if (total_nodes <= 0)
    {
        free_list_of_pairs(dist_result->head);
        return Result;
    }

    PairElement *RawArray = convert_list_to_array(dist_result->head, total_nodes);
    free_list_of_pairs(dist_result->head);
    dist_result->head = NULL;
    // Ordena o vetor lexicograficamente via Merge Sort O(M log M)
    merge_sort_pairs_elements(RawArray, 0, total_nodes - 1);

    PairElement *UniqueArray = (PairElement *)malloc(sizeof(PairElement) * total_nodes);
    if (UniqueArray == NULL)
    {
        fprintf(stderr, "Erro de alocacao de memoria para UniqueArray.\n");
        exit(1);
    }

    int unique_count = 0;
    UniqueArray[0] = RawArray[0];
    unique_count++;

    for (int i = 1; i < total_nodes; i++)
    {
        if (RawArray[i].i != RawArray[i - 1].i || RawArray[i].j != RawArray[i - 1].j)
        {
            UniqueArray[unique_count] = RawArray[i];
            unique_count++;
        }
    }

    free(RawArray);

    Result.pairs = UniqueArray;
    Result.num_pairs = unique_count;

    return Result;
}

DistResult min_points_distance_for_small_array(Point Points[], int array_lenght)
{
    Pair *PairsHead = NULL; // linked list of pairs of points with minimum distance
    lint d = LLONG_MAX;

    // caso de borda, não há distância a ser calculada com um ponto apenas
    if (array_lenght < 2)
    {
        DistResult result;
        result.min_dist = d;
        result.head = NULL;
        return result;
    }

    // força bruta para calcular a menor distância entre todos os pares de pontos fornecidos em um array pequeno (até 3 pontos)
    // Caso um vador menor que d seja encontrado, resetar a lista e adicionar o par
    // Caso um valor igual a d seja encontrado, adicionar o par à lista
    for (int i = 0; i < array_lenght - 1; i++)
    {
        for (int j = i + 1; j < array_lenght; j++)
        {
#ifndef DISTANCE_TYPE_1D
            lint dist = calc_2d_dist_square(&Points[i], &Points[j]);
#else
            lint dist = calc_1d_dist(&Points[i], &Points[j]);
#endif

            if (dist < d)
            {
                d = dist;
                free_list_of_pairs(PairsHead);
                PairsHead = NULL;
                PairsHead = add_pair_to_list(PairsHead, Points[i].id, Points[j].id);
            }
            else if (dist == d)
            {
                PairsHead = add_pair_to_list(PairsHead, Points[i].id, Points[j].id);
            }
        }
    }
    DistResult result;
    result.min_dist = d;
    result.head = PairsHead;
    return result;
}

DistResult min_distance_x_rec(Point Points[], int array_lenght)
{
    if (array_lenght <= 3)
    {
        return min_points_distance_for_small_array(Points, array_lenght);
    }

    int mean = array_lenght / 2;

    // Divisão
    Point *Pleft = (Point *)malloc(sizeof(Point) * mean);
    Point *Pright = (Point *)malloc(sizeof(Point) * (array_lenght - mean));
    for (int i = 0; i < mean; i++)
    {
        Pleft[i] = Points[i];
    }
    for (int i = mean; i < array_lenght; i++)
    {
        Pright[i - mean] = Points[i];
    }

    // Conquista
    DistResult left_result = min_distance_x_rec(Pleft, mean);
    DistResult right_result = min_distance_x_rec(Pright, array_lenght - mean);

    // Combinação

    // 1. Combinar os melhores resultados de cada metade

    lint d = min(left_result.min_dist, right_result.min_dist);
    Pair *PairsCombined = NULL;

    if (left_result.min_dist < right_result.min_dist)
    {
        printf("\t--left_result.min_dist: %lld\n", left_result.min_dist);
        PairsCombined = left_result.head;
        free_list_of_pairs(right_result.head);
    }
    else if (right_result.min_dist < left_result.min_dist)
    {
        printf("\t--right_result.min_dist: %lld\n", right_result.min_dist);
        PairsCombined = right_result.head;
        free_list_of_pairs(left_result.head);
    }
    else
    { // left_result.min_dist == right_result.min_dist
        printf("\t--left_result.min_dist == right_result.min_dist: %lld\n", left_result.min_dist);
        PairsCombined = merge_lists_of_pairs(left_result.head, right_result.head);
    }

    // 2. Combinar os melhores resultados da intersecção da direita com a esquerda
    lint x_mid = Points[mean].x;
    printf("\t--x_mid: %lld\n", x_mid);

    // 2.1 Filtrar os pontos de Points que estão dentro da faixa de distância d do ponto médio
    // array_length é o tamanho máximo possível da faixa, caso todos os pontos estejam dentro da faixa
    Point *Strip = (Point *)malloc(sizeof(Point) * array_lenght);
    int strip_size = 0;
    for (int i = 0; i < array_lenght; i++)
    {
        lint x_dist_to_mid = long_abs(Points[i].x - x_mid);
        if (x_dist_to_mid <= d)
        {
            printf("\t--Point[i].x dist to mid is %lld <= %lld\n", x_dist_to_mid, d);
            Strip[strip_size] = Points[i];
            strip_size++;
        }
    }
    // 2.2  Varia todos os pares dentro da faixa para ver se achamos distância <= d
    //      (No caso 1D no eixo X, basta comparar elementos na faixa)
    for (int i = 0; i < strip_size; i++)
    {
        for (int j = i + 1; j < strip_size; j++)
        {
            // como os elementos em x foram previamente ordenados, se a distância mensurada for maior que d, podemos interromper o laço
            lint dist = calc_1d_dist(&Strip[i], &Strip[j]);
            if (dist > d)
            {
                break; // não há necessidade de continuar a busca, pois os elementos estão ordenados em x
            }

            dist = calc_1d_dist(&Strip[i], &Strip[j]);
            if (dist < d)
            {
                d = dist;
                free_list_of_pairs(PairsCombined);
                PairsCombined = NULL;
                PairsCombined = add_pair_to_list(PairsCombined, Strip[i].id, Strip[j].id);
            }
            else if (dist == d)
            {
                PairsCombined = add_pair_to_list(PairsCombined, Strip[i].id, Strip[j].id);
            }
        }
    }
    free(Pleft);
    free(Pright);
    free(Strip);
    DistResult result;
    result.min_dist = d;
    result.head = PairsCombined;
    return result;
}

FinalResult min_distance_x(Point Points[], int array_lenght)
{
    // Ordenar antes de chamar. Permite  a chamada do break no duplo laço for em min_distance_x_rec()  pois os elementos estarão ordenados em x
    merge_sort_x_points(Points, 0, array_lenght - 1);
    // Divisão e Conquista
    DistResult result = min_distance_x_rec(Points, array_lenght);
    // "limpeza" da resposta em complexidade O(m*log(m)), o que não degrada a complexidade assintótica.
    // m será o numero total de pares de pontos com a menor distância, que é no máximo n(n-1)/2, ou seja, O(n^2)
    // porém em situações concretas, no caso bidimensional, condições de empacotamento limitam o número de pares
    // de pontos com a menor distância, de forma que na pratica m << n(n-1)/2
    FinalResult final_result = dist_result_to_final_result(&result);
    return final_result;
}

/**
MIN-DISTANCE-1D(Point P):
    # 1. Ordena inicialmente por X
    Px = MERGE-SORT-X(P)

    # 2. Executa o algoritmo de Divisão e Conquista
    (d, PairsCombined) = MIN-DISTANCE-1D-REC(Px)

    # 3. PÓS-PROCESSAMENTO: Normalização, Deduplicação e Ordenação Lexicográfica
    PairsNormalized = POST-PROCESS(PairsCombined)

    return (d, PairsSorted)


MIN-DISTANCE-1D-REC(Point Px):
    # Caso Base: para 3 ou menos pontos, resolve de forma direta (força bruta)
    if Px.length <= 3:
        return MIN-DISTANCE-1D-SMALL(Px)

    mean = floor(Px.length / 2)
    end = Px.length - 1

    # Divisão
    Pleft  = Px[0 : mean - 1]
    Pright = Px[mean : end]

    # Conquista
    (dleft, PairsListLeft)   = MIN-DISTANCE-1D-REC(Pleft)
    (dright, PairsListRight) = MIN-DISTANCE-1D-REC(Pright)

    # ----------------------------------------------------
    # COMBINAÇÃO - PARTE 1: Melhor resultado dos lados
    # ----------------------------------------------------
    d = min(dleft, dright)
    PairsCombined = []

    if dleft < dright:
        PairsCombined = PairsListLeft
    else if dright < dleft:
        PairsCombined = PairsListRight
    else: # dleft == dright (empate entre lados)
        PairsCombined = PairsListLeft + PairsListRight

    # ----------------------------------------------------
    # COMBINAÇÃO - PARTE 2: A Faixa Central (Intersecção)
    # ----------------------------------------------------
    X_med = Px[mean].x

    # 1. Filtra os pontos de Px que estão na faixa [X_med - d, X_med + d]
    Point Strip = []
    for point in Px:
        if abs(point.x - X_med) <= d:
            Strip.append(point)

    # 2. Varre todos os pares dentro da faixa
    for i = 0 to Strip.length - 1:
        for j = i + 1 to Strip.length - 1:

            # Se a distância em X já for maior que d, interrompe o laço j
            if (Strip[j].x - Strip[i].x) > d:
                break

            dist = CALCULATE_DIST_1D(Strip[i], Strip[j])

            if dist < d:
                # Achamos uma distância ESTRITAMENTE MENOR!
                d = dist
                PairsCombined = [ (Strip[i].id, Strip[j].id) ]

            else if dist == d:
                # Empate com a menor distância atual!
                PairsCombined.append( (Strip[i].id, Strip[j].id) )

    return (d, PairsCombined)

POST-PROCESS(PairsCombined):
    PairsNormalized = []
    for (id_a, id_b) in PairsCombined:
        # Garante a ordem interna do par: (menor_id, maior_id)
        if id_a < id_b:
            PairsNormalized.append((id_a, id_b))
        else:
            PairsNormalized.append((id_b, id_a))

    # Remove pares duplicados
    PairsUnique = REMOVE-DUPLICATES(PairsNormalized)

    # Ordena a lista lexicograficamente pelo id_a e depois id_b
    PairsSorted = SORT-LEXICOGRAPHICAL(PairsUnique)

    return PairsSorted

 */

/*
MIN-DISTANCE-2D(Point P):
   # 1. Ordena inicialmente por X para permitir a divisão ao meio
   Px = MERGE-SORT-X(P)

   # 2. Executa o algoritmo de Divisão e Conquista 2D
   (d, PairsCombined) = MIN-DISTANCE-2D-REC(Px)

   # 3. PÓS-PROCESSAMENTO: Normalização, Deduplicação e Ordenação Lexicográfica
   PairsSorted = POST-PROCESS(PairsCombined)

   return (d, PairsSorted)


MIN-DISTANCE-2D-REC(Point Px):
   # Caso Base: para 3 ou menos pontos, resolve por força bruta (Euclidiana 2D)
   if Px.length <= 3:
       return MIN-DISTANCE-2D-SMALL(Px)

   mean = floor(Px.length / 2)
   end = Px.length - 1

   # Divisão (fatias à esquerda e à direita da mediana em X)
   Pleft  = Px[0 : mean - 1]
   Pright = Px[mean : end]

   # Conquista
   (dleft, PairsListLeft)   = MIN-DISTANCE-2D-REC(Pleft)
   (dright, PairsListRight) = MIN-DISTANCE-2D-REC(Pright)

   # ----------------------------------------------------
   # COMBINAÇÃO - PARTE 1: Melhor resultado dos lados
   # ----------------------------------------------------
   d = min(dleft, dright)
   PairsCombined = []

   if dleft < dright:
       PairsCombined = PairsListLeft
   else if dright < dleft:
       PairsCombined = PairsListRight
   else: # dleft == dright (empate entre lados)
       PairsCombined = PairsListLeft + PairsListRight

   # ----------------------------------------------------
   # COMBINAÇÃO - PARTE 2: A Faixa Central (Strip 2D)
   # ----------------------------------------------------
   X_med = Px[mean].x

   # 1. Filtra os pontos de Px na faixa horizontal [X_med - d, X_med + d]
   Point Strip = []
   for point in Px:
       if abs(point.x - X_med) <= d:
           Strip.append(point)

   # 2. ORDENAÇÃO FUNDAMENTAL NO 2D:
   # Ordena os pontos da faixa pela coordenada Y para varredura vertical
   Strip = MERGE-SORT-Y(Strip)

   # 3. Varredura Geométrica na Faixa Central (Limitada pelo Eixo Y)
   for i = 0 to Strip.length - 1:
       for j = i + 1 to Strip.length - 1:

           # CRITÉRIO DE PARADA EM Y:
           # Como Strip está ordenada por Y, no momento em que (y_j - y_i) > d,
           # nenhum ponto seguinte estará a menos de d no plano.
           if (Strip[j].y - Strip[i].y) > d:
               break

           # Distância Euclidiana 2D: sqrt((x1 - x2)^2 + (y1 - y2)^2)
           dist = CALCULATE_DIST_2D(Strip[i], Strip[j])

           if dist < d:
               # Achamos uma distância ESTRITAMENTE MENOR!
               d = dist
               PairsCombined = [ (Strip[i].id, Strip[j].id) ]

           else if dist == d:
               # Empate com a menor distância atual!
               PairsCombined.append( (Strip[i].id, Strip[j].id) )

   return (d, PairsCombined)


POST-PROCESS(PairsCombined):
   PairsNormalized = []
   for (id_a, id_b) in PairsCombined:
       # Garante a ordem interna do par: (menor_id, maior_id)
       if id_a < id_b:
           PairsNormalized.append((id_a, id_b))
       else:
           PairsNormalized.append((id_b, id_a))

   # Remove pares duplicados
   PairsUnique = REMOVE-DUPLICATES(PairsNormalized)

   # Ordena a lista lexicograficamente pelo id_a e depois id_b
   PairsSorted = SORT-LEXICOGRAPHICAL(PairsUnique)

   return PairsSorted
*/