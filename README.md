# Bora?: Recomendação de Locais Próximos em São Paulo

Projeto da disciplina Teoria dos Grafos, Parte 3 (6º semestre de Ciência da Computação, Universidade Presbiteriana Mackenzie, Prof. Dr. Ivan Carlos Alcântara de Oliveira).

Repositório: https://github.com/anajuliaym/GrafosTG

## Integrantes

| Nome | RA | E-mail |
| --- | --- | --- |
| Gabriel Mires Camargo | 10436741 | 10436741@mackenzista.com.br |
| Ana Julia Yaguti Matilha | 10436655 | 10436655@mackenzista.com.br |
| Jiye Huang | 10438990 | 10438990@mackenzista.com.br |

## Problema

O Bora? conecta pessoas com interesses em comum para atividades presenciais em São Paulo. O projeto modela 118 locais reais (parques, museus, bares, espaços esportivos, entre outros) como um grafo em que as arestas representam proximidade, para responder: quais locais de uma categoria estão a até 1, 3 ou 5 km de um ponto?

## Modelagem

- **Tipo do grafo:** 2, não orientado com peso na aresta.
- **Vértices (118):** só locais reais, com rótulo `Nome [Categoria]` (70 coletados no Google My Maps na Parte 2 e 48 incluídos na Parte 3, com coordenadas do OpenStreetMap). A região é atributo do local, não vértice.
- **Arestas (299):** 298 de proximidade (4 vizinhos mais próximos, peso = distância de Haversine em km) e 1 de ligação entre agrupamentos, escolhida pela menor distância (regra de Kruskal).
- **Representação:** matriz de adjacência (`double[][]`), com infinito indicando ausência de aresta.

## Arquivos

```
├── Main.java               título e menu de opções (a até m)
├── Grafo.java              matriz de adjacência, operações, conexidade, Dijkstra e características
├── Endereco.java           vértice: nome, rótulo e peso
├── grafo.txt               grafo modelado (118 vértices, 299 arestas)
├── gerar_grafo.py          gera o grafo.txt a partir do KMZ
├── Grafo de Locais.kmz     dados coletados no Google My Maps
└── docs/
    ├── Relatorio_Projeto_Parte3_Bora.docx / .pdf
    └── Apresentacao_Bora_Parte3.pptx / .pdf
```

## Como executar

Requer Java 17 ou superior. Na raiz do repositório:

```bash
javac -encoding UTF-8 -d out *.java
java -cp out Main
```

Para regenerar o `grafo.txt` a partir do KMZ (Python 3, sem dependências externas):

```bash
python3 gerar_grafo.py
```

## Menu de opções

| Opção | Descrição |
| --- | --- |
| a / b | Ler / gravar o arquivo grafo.txt |
| c / d | Inserir vértice / inserir aresta |
| e / f | Remover vértice / remover aresta |
| g / h | Mostrar conteúdo do arquivo / mostrar grafo |
| i | Conexidade do grafo e grafo reduzido |
| j | Buscar locais próximos a um local, por raio e categoria (Dijkstra) |
| k | Menor caminho entre dois locais (Dijkstra) |
| l | Características: 1 graus, 2 coloração por classe (Welsh e Powell) e número cromático, 3 Euler, 4 Hamilton |
| m | Encerrar a aplicação |

## Resultados

- **Conexidade:** conexo, 1 componente (na Parte 2 eram 5).
- **Graus:** mínimo 4, máximo 9, média 5,07.
- **Coloração:** 5 classes e maior clique com 5 locais, logo X(G) = 5.
- **Euler:** 48 vértices de grau ímpar, sem caminho euleriano.
- **Hamilton:** não hamiltoniano (11 vértices de articulação).
- **Exemplo:** Museu do Ipiranga até Parque Ibirapuera, 7,05 km em 9 trechos.
