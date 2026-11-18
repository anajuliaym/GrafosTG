/*
 * Bora? - Grafo de Locais de Interesse da Cidade de São Paulo
 * Teoria dos Grafos - 6º CC - Universidade Presbiteriana Mackenzie
 *
 * Integrantes:
 *   Gabriel Mires Camargo     RA 10436741  10436741@mackenzista.com.br
 *   Ana Julia Yaguti Matilha  RA 10436655  10436655@mackenzista.com.br
 *   Jiye Huang                RA 10438990  10438990@mackenzista.com.br
 *
 * Síntese:
 *   Grafo representado por matriz de adjacência (double[][], +Infinito = sem
 *   aresta), com vetor de vértices (Endereco). Implementa leitura/gravação do
 *   grafo.txt, inserção/remoção de vértices e arestas, exibição, conexidade
 *   (fecho transitivo, C0-C3 e grafo reduzido) e, na Parte 3, a busca por
 *   proximidade e o menor caminho entre locais (Dijkstra) e as características
 *   do grafo: graus, coloração por classe (Welsh e Powell), Euler e Hamilton.
 *
 * Histórico de alterações:
 *   28/09/2026  Gabriel Mires Camargo     Versão inicial (Parte 2): matriz de
 *                                         adjacência e operações das opções a até h.
 *   28/09/2026  Ana Julia Yaguti Matilha  Conexidade por fecho transitivo (C0 a C3)
 *                                         e grafo reduzido (opção i); organização.
 *   04/11/2026  Ana Julia Yaguti Matilha  Parte 3: Dijkstra restrito aos locais
 *                                         (busca por raio e categoria, menor caminho).
 *   18/11/2026  Ana Julia Yaguti Matilha  Parte 3: graus, coloração por classe,
 *                                         maior clique, Euler e Hamilton com os
 *                                         teoremas vistos em aula; Dijkstra sobre
 *                                         todos os vértices (sem vértices de região).
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Grafo {

    // Tipo do Grafo (0 a 7), conforme definido no enunciado do projeto
    private int tipo;

    // Vetor de objetos Endereco: um Endereco por vértice (local)
    private ArrayList<Endereco> vertices;

    // Matriz de adjacência: matriz[i][j] = peso da aresta i->j, ou +Infinito se não existe
    private double[][] matriz;

    private static final double SEM_ARESTA = Double.POSITIVE_INFINITY;

    public Grafo() {
        this.tipo = 0;
        this.vertices = new ArrayList<>();
        this.matriz = new double[0][0];
    }

    // ---------------------------------------------------------------------
    // Helpers sobre o Tipo do Grafo
    // ---------------------------------------------------------------------

    public boolean isDirecionado() {
        return tipo >= 4;
    }

    public boolean temPesoVertice() {
        return tipo == 1 || tipo == 3 || tipo == 5 || tipo == 7;
    }

    public boolean temPesoAresta() {
        return tipo == 2 || tipo == 3 || tipo == 6 || tipo == 7;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public int numVertices() {
        return vertices.size();
    }

    public ArrayList<Endereco> getVertices() {
        return vertices;
    }

    /**
     * Conta o número de arestas atualmente presentes na matriz (m), respeitando
     * se o grafo é direcionado (conta cada entrada != SEM_ARESTA) ou não
     * direcionado (conta cada par {i,j} uma única vez, pois a matriz é
     * simétrica nesse caso).
     */
    public int numArestas() {
        int n = numVertices();
        int count = 0;
        for (int i = 0; i < n; i++) {
            int jIni = isDirecionado() ? 0 : i; // não direcionado: evita contar (i,j) e (j,i) duas vezes
            for (int j = jIni; j < n; j++) {
                if (i == j) continue;
                if (matriz[i][j] != SEM_ARESTA) count++;
            }
        }
        return count;
    }

    // ---------------------------------------------------------------------
    // a) Ler dados do arquivo grafo.txt
    // ---------------------------------------------------------------------

    private static final Pattern PADRAO_VERTICE =
            Pattern.compile("^(\\d+)\\s+\"(.*)\"\\s+(\\S+)\\s*$");

    public void lerArquivo(String caminho) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            tipo = Integer.parseInt(br.readLine().trim());

            int n = Integer.parseInt(br.readLine().trim());
            ArrayList<Endereco> novosVertices = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                String linha = br.readLine();
                Matcher m = PADRAO_VERTICE.matcher(linha);
                if (!m.matches()) {
                    throw new IOException("Linha de vértice em formato inválido: " + linha);
                }
                int idNoArquivo = Integer.parseInt(m.group(1));
                if (idNoArquivo != i) {
                    throw new IOException("Id do vértice fora de ordem na linha " + (i + 1)
                            + ": esperado " + i + ", encontrado " + idNoArquivo
                            + " (o id deve coincidir com a posição no vetor).");
                }
                String rotulo = m.group(2);
                double peso = Double.parseDouble(m.group(3));
                novosVertices.add(new Endereco(rotulo, peso)); // id = posição i no vetor, não é guardado no objeto
            }

            int mArestas = Integer.parseInt(br.readLine().trim());
            double[][] novaMatriz = criarMatrizVazia(n);
            for (int i = 0; i < mArestas; i++) {
                String linha = br.readLine();
                if (linha == null || linha.trim().isEmpty()) continue;
                String[] partes = linha.trim().split("\\s+");
                int origem = Integer.parseInt(partes[0]);
                int destino = Integer.parseInt(partes[1]);
                double peso = (partes.length >= 3) ? Double.parseDouble(partes[2]) : 0.0;

                novaMatriz[origem][destino] = peso;
                if (!isDirecionado()) {
                    novaMatriz[destino][origem] = peso;
                }
            }

            this.vertices = novosVertices;
            this.matriz = novaMatriz;
        }
    }

    private double[][] criarMatrizVazia(int n) {
        double[][] m = new double[n][n];
        for (double[] linha : m) {
            java.util.Arrays.fill(linha, SEM_ARESTA);
        }
        return m;
    }

    // ---------------------------------------------------------------------
    // b) Gravar dados no arquivo grafo.txt (mesmo formato da leitura)
    // ---------------------------------------------------------------------

    public void gravarArquivo(String caminho) throws IOException {
        try (FileWriter fw = new FileWriter(caminho)) {
            fw.write(tipo + "\n");
            fw.write(numVertices() + "\n");
            for (int i = 0; i < numVertices(); i++) {
                fw.write(vertices.get(i).toLinhaArquivo(i) + "\n");
            }

            List<String> linhasArestas = new ArrayList<>();
            int n = numVertices();
            for (int i = 0; i < n; i++) {
                int jIni = isDirecionado() ? 0 : i;
                for (int j = jIni; j < n; j++) {
                    if (i == j) continue;
                    if (matriz[i][j] != SEM_ARESTA) {
                        linhasArestas.add(i + " " + j + " " + Endereco.formatarPeso(matriz[i][j]));
                    }
                }
            }

            fw.write(linhasArestas.size() + "\n");
            for (String linha : linhasArestas) {
                fw.write(linha + "\n");
            }
        }
    }

    // ---------------------------------------------------------------------
    // c) Inserir vértice
    // ---------------------------------------------------------------------

    /** Insere um vértice a partir apenas do rótulo completo (endereço é derivado automaticamente). */
    public int inserirVertice(String rotulo, double peso) {
        return inserirVerticeInterno(new Endereco(rotulo, peso));
    }

    /** Insere um vértice informando endereço (nome/apelido) e rótulo completo separadamente. */
    public int inserirVertice(String endereco, String rotulo, double peso) {
        return inserirVerticeInterno(new Endereco(endereco, rotulo, peso));
    }

    private int inserirVerticeInterno(Endereco novoEndereco) {
        int n = numVertices(); // tamanho antigo = id que o novo vértice vai receber
        vertices.add(novoEndereco); // fica na posição n do vetor => seu "id" passa a ser n

        double[][] novaMatriz = criarMatrizVazia(n + 1);
        for (int i = 0; i < n; i++) {
            System.arraycopy(matriz[i], 0, novaMatriz[i], 0, n);
        }
        matriz = novaMatriz;
        return n;
    }

    // ---------------------------------------------------------------------
    // d) Inserir aresta
    // ---------------------------------------------------------------------

    public boolean inserirAresta(int origem, int destino, double peso) {
        if (!verticeValido(origem) || !verticeValido(destino) || origem == destino) return false;
        matriz[origem][destino] = peso;
        if (!isDirecionado()) {
            matriz[destino][origem] = peso;
        }
        return true;
    }

    // ---------------------------------------------------------------------
    // e) Remover vértice (e todas as arestas ligadas a ele)
    // ---------------------------------------------------------------------

    public boolean removerVertice(int id) {
        if (!verticeValido(id)) return false;

        vertices.remove(id);
        // Não é preciso reindexar nada nos objetos Endereco: como eles não guardam
        // id próprio, os vértices que vinham depois automaticamente "viram" o id
        // anterior menos 1, só por terem deslizado uma posição no ArrayList.

        int n = matriz.length;
        double[][] novaMatriz = criarMatrizVazia(n - 1);
        for (int i = 0, ni = 0; i < n; i++) {
            if (i == id) continue;
            for (int j = 0, nj = 0; j < n; j++) {
                if (j == id) continue;
                novaMatriz[ni][nj] = matriz[i][j];
                nj++;
            }
            ni++;
        }
        matriz = novaMatriz;
        return true;
    }

    // ---------------------------------------------------------------------
    // f) Remover aresta
    // ---------------------------------------------------------------------

    public boolean removerAresta(int origem, int destino) {
        if (!verticeValido(origem) || !verticeValido(destino)) return false;
        boolean existia = matriz[origem][destino] != SEM_ARESTA;
        matriz[origem][destino] = SEM_ARESTA;
        if (!isDirecionado()) {
            matriz[destino][origem] = SEM_ARESTA;
        }
        return existia;
    }

    private boolean verticeValido(int id) {
        return id >= 0 && id < numVertices();
    }

    // ---------------------------------------------------------------------
    // g) Mostrar conteúdo do arquivo (tipo, vértices e arestas) de forma legível
    // ---------------------------------------------------------------------

    public String mostrarConteudoArquivo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tipo do grafo: ").append(tipo).append(" - ").append(descreverTipo(tipo)).append("\n");
        sb.append("Quantidade de vértices (n): ").append(numVertices()).append("\n");
        sb.append("Quantidade de arestas (m): ").append(numArestas()).append("\n\n");

        sb.append("--- Vértices ---\n");
        for (int i = 0; i < numVertices(); i++) {
            Endereco e = vertices.get(i);
            sb.append(String.format("  [%3d] %-55s peso=%s%n",
                    i, e.getRotulo(), Endereco.formatarPeso(e.getPeso())));
        }

        sb.append("\n--- Arestas ---\n");
        int n = numVertices();
        for (int i = 0; i < n; i++) {
            int jIni = isDirecionado() ? 0 : i;
            for (int j = jIni; j < n; j++) {
                if (i == j) continue;
                if (matriz[i][j] != SEM_ARESTA) {
                    String seta = isDirecionado() ? " -> " : " -- ";
                    sb.append(String.format("  %3d%s%-3d  peso=%s%n",
                            i, seta, j, Endereco.formatarPeso(matriz[i][j])));
                }
            }
        }
        return sb.toString();
    }

    public static String descreverTipo(int tipo) {
        switch (tipo) {
            case 0: return "grafo não orientado sem peso";
            case 1: return "grafo não orientado com peso no vértice";
            case 2: return "grafo não orientado com peso na aresta";
            case 3: return "grafo não orientado com peso nos vértices e arestas";
            case 4: return "grafo orientado sem peso";
            case 5: return "grafo orientado com peso no vértice";
            case 6: return "grafo orientado com peso na aresta";
            case 7: return "grafo orientado com peso nos vértices e arestas";
            default: return "tipo desconhecido";
        }
    }

    // ---------------------------------------------------------------------
    // h) Mostrar o grafo (lista de adjacência derivada da matriz, mais legível
    //    que a matriz completa quando n é grande - ex.: 80 vértices)
    // ---------------------------------------------------------------------

    public String mostrarGrafo() {
        StringBuilder sb = new StringBuilder();
        int n = numVertices();
        for (int i = 0; i < n; i++) {
            sb.append(String.format("[%3d] %s:%n", i, vertices.get(i).getRotulo()));
            boolean algumVizinho = false;
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                if (matriz[i][j] != SEM_ARESTA) {
                    algumVizinho = true;
                    sb.append(String.format("        -> [%3d] %-45s (peso=%s)%n",
                            j, vertices.get(j).getRotulo(), Endereco.formatarPeso(matriz[i][j])));
                }
            }
            if (!algumVizinho) {
                sb.append("        (sem vizinhos)\n");
            }
        }
        return sb.toString();
    }

    /** Imprime a matriz de adjacência "crua" (útil para grafos pequenos de teste). */
    public String mostrarMatriz() {
        StringBuilder sb = new StringBuilder();
        int n = numVertices();
        sb.append("     ");
        for (int j = 0; j < n; j++) sb.append(String.format("%6d", j));
        sb.append("\n");
        for (int i = 0; i < n; i++) {
            sb.append(String.format("%4d ", i));
            for (int j = 0; j < n; j++) {
                if (matriz[i][j] == SEM_ARESTA) sb.append("     -");
                else sb.append(String.format("%6s", Endereco.formatarPeso(matriz[i][j])));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------------
    // i) Conexidade (C0-C3) e Grafo Reduzido
    //
    // Implementado com a técnica de FECHO DIRETO/INVERSO por ponto fixo,
    // exatamente como ensinado em aula (Ex15, Ex16 e Ex17): em vez de uma
    // busca em profundidade clássica (Kosaraju), repete-se a marcação de
    // vizinhos até não conseguir marcar mais ninguém novo. É um pouco menos
    // eficiente que Kosaraju (O(n^3) em vez de O(n^2)), mas é o algoritmo
    // apresentado na disciplina e mais fácil de explicar na apresentação.
    // ---------------------------------------------------------------------

    /** Resultado da análise de conexidade, para exibição no menu. */
    public static class ResultadoConexidade {
        public String classificacao;           // "Conexo"/"Desconexo" (não direcionado) ou "C0".."C3" (direcionado)
        public List<List<Integer>> componentes; // componentes conexas / fortemente conexas
        public String grafoReduzidoTexto;       // representação textual do grafo reduzido (só p/ direcionado)
    }

    /**
     * R+(v): conjunto de vértices alcançáveis a PARTIR de v (inclui o próprio v),
     * seguindo o sentido das arestas. Fecho transitivo direto por ponto fixo
     * (mesma técnica do método fechoDireto() do Ex16/Ex17).
     */
    private boolean[] fechoDireto(int v) {
        int n = numVertices();
        boolean[] r = new boolean[n];
        r[v] = true;
        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (r[i] && matriz[i][j] != SEM_ARESTA && !r[j]) {
                        r[j] = true;
                        mudou = true;
                    }
                }
            }
        }
        return r;
    }

    /**
     * R-(v): conjunto de vértices que CONSEGUEM CHEGAR em v (inclui o próprio v).
     * Fecho transitivo inverso por ponto fixo (mesma técnica do fechoInverso() do Ex16/Ex17).
     */
    private boolean[] fechoInverso(int v) {
        int n = numVertices();
        boolean[] r = new boolean[n];
        r[v] = true;
        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (r[j] && matriz[i][j] != SEM_ARESTA && !r[i]) {
                        r[i] = true;
                        mudou = true;
                    }
                }
            }
        }
        return r;
    }

    public ResultadoConexidade apresentarConexidade() {
        ResultadoConexidade r = new ResultadoConexidade();
        if (!isDirecionado()) {
            r.componentes = componentesConexasNaoDirecionado();
            r.classificacao = (r.componentes.size() <= 1) ? "Conexo" : "Desconexo (" + r.componentes.size() + " componentes)";
            r.grafoReduzidoTexto = null; // reduzido só se aplica a componentes fortemente conexas (grafo direcionado)
            return r;
        }

        // Grafo direcionado: usa fechoDireto/fechoInverso para achar as
        // Componentes Fortemente Conexas e classificar em C0..C3 (Ex16/Ex17).
        List<List<Integer>> scc = componentesFortementeConexas();
        r.componentes = scc;

        if (isFortementeConexo()) {
            r.classificacao = "C3 (fortemente conexo)";
        } else if (isUnilateralmenteConexo()) {
            r.classificacao = "C2 (unilateralmente conexo)";
        } else if (isConexoIgnorandoDirecao()) {
            r.classificacao = "C1 (conexo / fracamente conexo)";
        } else {
            r.classificacao = "C0 (desconexo)";
        }

        r.grafoReduzidoTexto = montarTextoGrafoReduzido(scc);
        return r;
    }

    /**
     * Componentes conexas de um grafo NÃO direcionado: como a matriz é simétrica,
     * R+(v) já é exatamente a componente inteira de v (não precisa também do R-(v)).
     */
    private List<List<Integer>> componentesConexasNaoDirecionado() {
        int n = numVertices();
        boolean[] jaEstaEmComponente = new boolean[n];
        List<List<Integer>> componentes = new ArrayList<>();

        for (int v = 0; v < n; v++) {
            if (jaEstaEmComponente[v]) continue;
            boolean[] r = fechoDireto(v);
            List<Integer> comp = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (r[i]) {
                    comp.add(i);
                    jaEstaEmComponente[i] = true;
                }
            }
            componentes.add(comp);
        }
        return componentes;
    }

    /** C3 - fortemente conexo: de QUALQUER vértice dá pra chegar em todos os outros. */
    private boolean isFortementeConexo() {
        int n = numVertices();
        for (int v = 0; v < n; v++) {
            boolean[] r = fechoDireto(v);
            for (int i = 0; i < n; i++) {
                if (!r[i]) return false;
            }
        }
        return true;
    }

    /** C2 - unilateralmente conexo: para TODO par de vértices, pelo menos um alcança o outro. */
    private boolean isUnilateralmenteConexo() {
        int n = numVertices();
        for (int v = 0; v < n; v++) {
            boolean[] rd = fechoDireto(v);
            boolean[] ri = fechoInverso(v);
            for (int i = 0; i < n; i++) {
                if (!rd[i] && !ri[i]) return false;
            }
        }
        return true;
    }

    /** C1 - conexo ignorando a direção (fracamente conexo): ligando os arcos nos dois sentidos,
     *  dá pra ir de qualquer vértice a qualquer outro. */
    private boolean isConexoIgnorandoDirecao() {
        int n = numVertices();
        if (n == 0) return true;
        boolean[] visitado = new boolean[n];
        visitado[0] = true;
        boolean mudou = true;
        while (mudou) {
            mudou = false;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (visitado[i] && (matriz[i][j] != SEM_ARESTA || matriz[j][i] != SEM_ARESTA) && !visitado[j]) {
                        visitado[j] = true;
                        mudou = true;
                    }
                }
            }
        }
        for (boolean v : visitado) {
            if (!v) return false;
        }
        return true;
    }

    /**
     * Componentes Fortemente Conexas: a componente de v é a INTERSECÇÃO entre
     * R+(v) e R-(v), ou seja, os vértices que estão nos dois fechos ao mesmo
     * tempo (mesma lógica do componentesFConexas() do Ex17).
     */
    private List<List<Integer>> componentesFortementeConexas() {
        int n = numVertices();
        int[] idComponente = new int[n];
        java.util.Arrays.fill(idComponente, -1);
        List<List<Integer>> sccs = new ArrayList<>();
        int c = 0;

        for (int v = 0; v < n; v++) {
            if (idComponente[v] != -1) continue;
            boolean[] rd = fechoDireto(v);
            boolean[] ri = fechoInverso(v);
            List<Integer> compAtual = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (rd[i] && ri[i] && idComponente[i] == -1) {
                    idComponente[i] = c;
                    compAtual.add(i);
                }
            }
            sccs.add(compAtual);
            c++;
        }
        return sccs;
    }

    private int[] mapeiaVerticeParaComponente(List<List<Integer>> scc) {
        int[] idComponente = new int[numVertices()];
        for (int c = 0; c < scc.size(); c++) {
            for (int v : scc.get(c)) idComponente[v] = c;
        }
        return idComponente;
    }

    /**
     * Grafo reduzido: um vértice para cada componente fortemente conexa, com um
     * arco entre duas componentes quando existe pelo menos um arco entre elas no
     * grafo original (mesma ideia do grafoReduzido() do Ex17).
     */
    private String montarTextoGrafoReduzido(List<List<Integer>> scc) {
        if (scc.size() <= 1) {
            return "O grafo reduzido possui um único vértice (o grafo original já é fortemente conexo).";
        }
        int[] idComponente = mapeiaVerticeParaComponente(scc);
        int k = scc.size();
        boolean[][] cond = new boolean[k][k];
        int n = numVertices();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i != j && matriz[i][j] != SEM_ARESTA) {
                    int ci = idComponente[i], cj = idComponente[j];
                    if (ci != cj) cond[ci][cj] = true;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Vértices do grafo reduzido (cada um representa uma componente fortemente conexa):\n");
        for (int c = 0; c < scc.size(); c++) {
            sb.append(String.format("  S%d = { ", c));
            for (int v : scc.get(c)) {
                sb.append(vertices.get(v).getRotulo()).append("; ");
            }
            sb.append("}\n");
        }
        sb.append("Arestas do grafo reduzido:\n");
        boolean algumaAresta = false;
        for (int i = 0; i < k; i++) {
            for (int j = 0; j < k; j++) {
                if (cond[i][j]) {
                    sb.append(String.format("  S%d -> S%d%n", i, j));
                    algumaAresta = true;
                }
            }
        }
        if (!algumaAresta) sb.append("  (nenhuma)\n");
        return sb.toString();
    }

    // =====================================================================
    // PARTE 3
    // =====================================================================

    // ---------------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------------

    private boolean existeAresta(int i, int j) {
        return i != j && matriz[i][j] != SEM_ARESTA;
    }

    public boolean isVertice(int v) {
        return verticeValido(v);
    }

    /** Ids cujo rótulo contém o texto informado (sem diferenciar maiúsculas). */
    public List<Integer> procurarPorNome(String trecho) {
        List<Integer> ids = new ArrayList<>();
        String alvo = trecho.toLowerCase();
        for (int i = 0; i < numVertices(); i++) {
            if (vertices.get(i).getRotulo().toLowerCase().contains(alvo)) ids.add(i);
        }
        return ids;
    }

    public String rotulo(int v) {
        return vertices.get(v).getRotulo();
    }

    // ---------------------------------------------------------------------
    // j) e k) Caminho mínimo - algoritmo de Dijkstra
    //
    // Versão O(n^2) sobre a matriz de adjacência, como apresentada em aula:
    // a cada passo escolhe o vértice ainda aberto de menor estimativa d[v],
    // fecha-o e relaxa as arestas que saem dele.
    // ---------------------------------------------------------------------

    /** Resultado do Dijkstra a partir de uma origem: distâncias e predecessores. */
    public static class ResultadoDijkstra {
        public final int origem;
        public final double[] distancia;  // +Infinito = inalcançável
        public final int[] anterior;      // -1 = sem predecessor

        ResultadoDijkstra(int origem, double[] distancia, int[] anterior) {
            this.origem = origem;
            this.distancia = distancia;
            this.anterior = anterior;
        }

        /** Reconstrói o caminho origem -> destino seguindo os predecessores. */
        public List<Integer> caminhoAte(int destino) {
            List<Integer> caminho = new ArrayList<>();
            if (distancia[destino] == SEM_ARESTA) return caminho;
            for (int v = destino; v != -1; v = anterior[v]) caminho.add(v);
            Collections.reverse(caminho);
            return caminho;
        }
    }

    public ResultadoDijkstra dijkstra(int origem) {
        int n = numVertices();
        double[] d = new double[n];
        int[] anterior = new int[n];
        boolean[] fechado = new boolean[n];
        Arrays.fill(d, SEM_ARESTA);
        Arrays.fill(anterior, -1);
        d[origem] = 0.0;

        for (int passo = 0; passo < n; passo++) {
            // escolhe o vértice aberto de menor distância estimada
            int u = -1;
            for (int v = 0; v < n; v++) {
                if (!fechado[v] && d[v] != SEM_ARESTA && (u == -1 || d[v] < d[u])) u = v;
            }
            if (u == -1) break; // os restantes são inalcançáveis
            fechado[u] = true;

            // relaxamento das arestas (u, v)
            for (int v = 0; v < n; v++) {
                if (!fechado[v] && existeAresta(u, v) && d[u] + matriz[u][v] < d[v]) {
                    d[v] = d[u] + matriz[u][v];
                    anterior[v] = u;
                }
            }
        }
        return new ResultadoDijkstra(origem, d, anterior);
    }

    /** Um local encontrado pela busca por proximidade. */
    public static class LocalProximo {
        public final int id;
        public final double distanciaKm;
        public final int saltos; // quantidade de arestas no caminho mínimo

        LocalProximo(int id, double distanciaKm, int saltos) {
            this.id = id;
            this.distanciaKm = distanciaKm;
            this.saltos = saltos;
        }
    }

    /**
     * j) Locais a até raioKm da origem pela rede de proximidade, opcionalmente
     * só de uma categoria (null ou vazio = todas), em ordem de distância.
     */
    public List<LocalProximo> buscarProximos(int origem, double raioKm, String categoria) {
        ResultadoDijkstra r = dijkstra(origem);
        boolean filtrar = categoria != null && !categoria.isBlank();
        List<LocalProximo> lista = new ArrayList<>();
        for (int v = 0; v < numVertices(); v++) {
            if (v == origem || r.distancia[v] > raioKm) continue;
            if (filtrar && !categoria.trim().equalsIgnoreCase(vertices.get(v).getCategoria())) continue;
            lista.add(new LocalProximo(v, r.distancia[v], r.caminhoAte(v).size() - 1));
        }
        lista.sort((a, b) -> Double.compare(a.distanciaKm, b.distanciaKm));
        return lista;
    }

    /** Categorias distintas presentes nos vértices, em ordem alfabética. */
    public List<String> categorias() {
        List<String> cats = new ArrayList<>();
        for (Endereco e : vertices) {
            String c = e.getCategoria();
            if (c != null && !cats.contains(c)) cats.add(c);
        }
        Collections.sort(cats);
        return cats;
    }

    // ---------------------------------------------------------------------
    // l) Características do grafo
    // ---------------------------------------------------------------------

    /** Grau de v: número de arestas incidentes (não orientado) ou entrada+saída (orientado). */
    public int grau(int v) {
        int g = 0;
        for (int j = 0; j < numVertices(); j++) {
            if (existeAresta(v, j)) g++;
            if (isDirecionado() && existeAresta(j, v)) g++;
        }
        return g;
    }

    public String relatorioGraus() {
        int n = numVertices();
        StringBuilder sb = new StringBuilder();
        if (n == 0) return "Grafo vazio.\n";
        int soma = 0, min = Integer.MAX_VALUE, max = -1;
        java.util.TreeMap<Integer, Integer> distribuicao = new java.util.TreeMap<>();
        for (int v = 0; v < n; v++) {
            int g = grau(v);
            soma += g;
            min = Math.min(min, g);
            max = Math.max(max, g);
            distribuicao.merge(g, 1, Integer::sum);
        }
        int impares = 0;
        for (int v = 0; v < n; v++) if (grau(v) % 2 == 1) impares++;
        sb.append(String.format("Soma dos graus = %d = 2 x %d arestas (Teorema 1: a soma dos graus é o dobro do nº de arestas)%n",
                soma, numArestas()));
        sb.append(String.format("Vértices de grau ímpar: %d, número par (Teorema 2)%n", impares));
        sb.append(String.format("Grau mínimo δ(G) = %d | grau máximo Δ(G) = %d | grau médio = %.2f%n",
                min, max, (double) soma / n));
        sb.append("Distribuição (grau: quantidade de vértices):\n  ");
        for (var e : distribuicao.entrySet()) sb.append(e.getKey()).append(": ").append(e.getValue()).append("   ");
        sb.append("\nVértices de maior grau:\n");
        for (int v = 0; v < n; v++) {
            if (grau(v) == max) sb.append(String.format("  [%d] %s (grau %d)%n", v, rotulo(v), max));
        }
        return sb.toString();
    }

    /**
     * Coloração por Classe (algoritmo de Welsh e Powell, visto em aula), com os
     * vértices rotulados em ordem NÃO CRESCENTE de grau: abre a classe de cor k,
     * percorre os vértices ainda sem cor e coloca na classe k todo vértice que
     * não tem vizinho dentro dela; quando ninguém mais cabe, passa para k + 1.
     * Retorna as classes C1, C2, ... (listas de ids). A quantidade de classes é
     * um limite superior para o número cromático X(G).
     */
    public List<List<Integer>> coloracaoPorClasse() {
        int n = numVertices();
        List<Integer> w = new ArrayList<>(); // W: vértices que ainda não têm classe
        for (int i = 0; i < n; i++) w.add(i);
        w.sort((a, b) -> grau(b) != grau(a) ? grau(b) - grau(a) : a - b);

        List<List<Integer>> classes = new ArrayList<>();
        while (!w.isEmpty()) {
            List<Integer> ck = new ArrayList<>();     // classe atual Ck
            for (int v : new ArrayList<>(w)) {
                boolean temVizinhoNaClasse = false;
                for (int u : ck) if (existeAresta(v, u) || existeAresta(u, v)) { temVizinhoNaClasse = true; break; }
                if (!temVizinhoNaClasse) {     // N(v) ∩ Ck = vazio: v recebe a cor k
                    ck.add(v);
                    w.remove(Integer.valueOf(v));
                }
            }
            classes.add(ck);
        }
        return classes;
    }

    /**
     * Maior clique (conjunto de vértices todos vizinhos entre si), por busca
     * com retrocesso. Os vértices de uma clique precisam de cores diferentes,
     * então o tamanho da maior clique é um LIMITE INFERIOR para X(G).
     */
    public List<Integer> maiorClique() {
        List<Integer> melhor = new ArrayList<>();
        expandirClique(new ArrayList<>(), 0, melhor);
        return melhor;
    }

    private void expandirClique(List<Integer> atual, int proximo, List<Integer> melhor) {
        if (atual.size() > melhor.size()) { melhor.clear(); melhor.addAll(atual); }
        for (int v = proximo; v < numVertices(); v++) {
            boolean ligadoATodos = true;
            for (int u : atual) if (!existeAresta(u, v)) { ligadoATodos = false; break; }
            if (!ligadoATodos) continue;
            atual.add(v);
            expandirClique(atual, v + 1, melhor);
            atual.remove(atual.size() - 1);
        }
    }

    public String relatorioColoracao() {
        if (numVertices() == 0) return "Grafo vazio.\n";
        StringBuilder sb = new StringBuilder();
        List<List<Integer>> classes = coloracaoPorClasse();
        int k = classes.size();
        sb.append("Coloração por Classe (Welsh e Powell, vértices em ordem não crescente de grau): ")
          .append(k).append(" classes de cor, logo X(G) <= ").append(k).append(".\n");
        for (int c = 0; c < k; c++) {
            sb.append(String.format("  C%d: %d vértices %s%n", c + 1, classes.get(c).size(), classes.get(c)));
        }
        List<Integer> clique = maiorClique();
        sb.append("Maior clique: ").append(clique.size()).append(" vértices vizinhos entre si:\n");
        for (int v : clique) sb.append("  [").append(v).append("] ").append(rotulo(v)).append("\n");
        sb.append("Eles precisam de cores diferentes, logo X(G) >= ").append(clique.size()).append(".\n");
        if (clique.size() == k) {
            sb.append("=> Número cromático X(G) = ").append(k).append(" (o limite superior coincide com o inferior).\n");
        } else {
            sb.append("=> ").append(clique.size()).append(" <= X(G) <= ").append(k).append(".\n");
        }
        return sb.toString();
    }

    /** Conexo considerando apenas os vértices que têm alguma aresta (usado no teste de Euler). */
    private boolean conexoNosVerticesComAresta() {
        int n = numVertices();
        int inicio = -1;
        for (int v = 0; v < n && inicio == -1; v++) if (grau(v) > 0) inicio = v;
        if (inicio == -1) return true;
        boolean[] r = fechoDireto(inicio);
        for (int v = 0; v < n; v++) if (grau(v) > 0 && !r[v]) return false;
        return true;
    }

    /**
     * Euler (grafo não orientado): conexo e todos os graus pares => euleriano
     * (tem ciclo euleriano); conexo e exatamente 2 graus ímpares => semieuleriano
     * (tem percurso/trilha euleriana entre eles); caso contrário, nenhum dos dois.
     */
    public String relatorioEuler() {
        if (isDirecionado()) {
            return "Verificação de Euler implementada para grafos não orientados (o grafo do projeto é tipo 2).\n";
        }
        List<Integer> impares = new ArrayList<>();
        for (int v = 0; v < numVertices(); v++) if (grau(v) % 2 == 1) impares.add(v);
        boolean conexo = conexoNosVerticesComAresta();
        StringBuilder sb = new StringBuilder();
        sb.append("Conexo: ").append(conexo ? "sim" : "não").append("\n");
        sb.append("Vértices de grau ímpar: ").append(impares.size()).append(" ").append(impares).append("\n");
        if (conexo && impares.isEmpty()) {
            sb.append("=> EULERIANO (Teorema 3): tem caminho euleriano fechado, que usa cada aresta uma única vez.\n");
        } else if (conexo && impares.size() == 2) {
            sb.append("=> Tem caminho euleriano ABERTO (Teorema 3), começando em ")
              .append(impares.get(0)).append(" até ").append(impares.get(1)).append(".\n");
        } else {
            sb.append("=> NÃO tem caminho euleriano, nem aberto nem fechado (Teorema 3: só existe com 0 ou 2 vértices de grau ímpar).\n");
            if (conexo) sb.append("   Percorrer todas as arestas exigiria repetir trechos (problema do carteiro chinês).\n");
        }
        return sb.toString();
    }

    /**
     * Vértices de articulação: v é de articulação quando {v} é um subconjunto de
     * articulação, isto é, removendo v o restante do grafo fica desconexo.
     * Testa um vértice por vez: marca v como removido e faz uma busca a partir
     * de outro vértice; se algum vértice não for alcançado, v é de articulação.
     */
    public List<Integer> verticesDeArticulacao() {
        int n = numVertices();
        List<Integer> lista = new ArrayList<>();
        for (int v = 0; v < n; v++) {
            boolean[] alcancado = new boolean[n];
            alcancado[v] = true; // v "removido": a busca não passa por ele
            int inicio = (v == 0) ? 1 : 0;
            if (inicio >= n) break;
            java.util.ArrayDeque<Integer> fila = new java.util.ArrayDeque<>();
            fila.add(inicio);
            alcancado[inicio] = true;
            while (!fila.isEmpty()) {
                int u = fila.poll();
                for (int x = 0; x < n; x++) {
                    if (!alcancado[x] && existeAresta(u, x)) { alcancado[x] = true; fila.add(x); }
                }
            }
            for (int x = 0; x < n; x++) if (!alcancado[x]) { lista.add(v); break; }
        }
        return lista;
    }

    /** Resultado da busca de ciclo hamiltoniano. */
    public static class ResultadoHamilton {
        public String conclusao;
        public List<Integer> ciclo; // null quando não encontrado
    }

    /**
     * Hamilton: primeiro verifica condições NECESSÁRIAS (conexo, grau mínimo >= 2,
     * sem vértice de articulação) e os Teoremas 1 e 2 vistos em aula, que são
     * SUFICIENTES (d(u)+d(v) >= |V| para pares não adjacentes; grau >= |V|/2). Como para o grafo do projeto nenhuma decide a
     * questão, faz busca com retrocesso (backtracking) por um ciclo hamiltoniano,
     * escolhendo primeiro o vizinho com menos opções livres (heurística de
     * Warnsdorff) e podando ramos sem saída, com limite de tempo, já que o
     * problema é NP-completo.
     */
    public ResultadoHamilton verificarHamilton(long limiteMs) {
        ResultadoHamilton r = new ResultadoHamilton();
        int n = numVertices();
        if (isDirecionado()) { r.conclusao = "Verificação implementada para grafos não orientados."; return r; }
        if (n < 3) { r.conclusao = "Com menos de 3 vértices não há ciclo hamiltoniano."; return r; }

        StringBuilder sb = new StringBuilder();
        if (componentesConexasNaoDirecionado().size() > 1) {
            r.conclusao = "NÃO hamiltoniano: o grafo é desconexo.";
            return r;
        }
        int minGrau = Integer.MAX_VALUE;
        for (int v = 0; v < n; v++) minGrau = Math.min(minGrau, grau(v));
        if (minGrau < 2) {
            r.conclusao = "NÃO hamiltoniano: há vértice de grau " + minGrau + " (um ciclo usa 2 arestas de cada vértice).";
            return r;
        }
        List<Integer> art = verticesDeArticulacao();
        if (!art.isEmpty()) {
            r.conclusao = "NÃO hamiltoniano: há vértices de articulação " + art + " (um ciclo hamiltoniano exige grafo 2-conexo).";
            return r;
        }
        sb.append("Condições necessárias atendidas (conexo, δ(G) = ").append(minGrau)
          .append(" >= 2, sem vértice de articulação: grafo 2-conexo).\n");
        boolean dirac = minGrau * 2 >= n;
        boolean ore = true;
        for (int u = 0; u < n && ore; u++)
            for (int v = u + 1; v < n && ore; v++)
                if (!existeAresta(u, v) && grau(u) + grau(v) < n) ore = false;
        sb.append("Teorema 1 (d(u) + d(v) >= |V| = ").append(n).append(" para todo par não adjacente): ")
          .append(ore ? "atende" : "não atende").append("\n");
        sb.append("Teorema 2 (todo vértice com grau >= |V|/2 = ").append(n / 2).append("): ")
          .append(dirac ? "atende" : "não atende").append("\n");
        sb.append("(Os dois teoremas são condições suficientes, mas não necessárias; como não decidem, busca-se o ciclo.)\n");

        int[] caminho = new int[n];
        boolean[] usado = new boolean[n];
        caminho[0] = 0;
        usado[0] = true;
        long prazo = System.currentTimeMillis() + limiteMs;
        Boolean achou = buscarCicloHamiltoniano(caminho, usado, 1, prazo);
        if (achou == null) {
            sb.append("Busca com retrocesso interrompida após ").append(limiteMs).append(" ms: INCONCLUSIVO.");
        } else if (achou) {
            r.ciclo = new ArrayList<>();
            for (int v : caminho) r.ciclo.add(v);
            r.ciclo.add(caminho[0]);
            sb.append("=> HAMILTONIANO: encontrado ciclo que passa uma vez por cada um dos ").append(n).append(" vértices.");
        } else {
            sb.append("=> NÃO hamiltoniano: a busca exaustiva não encontrou ciclo.");
        }
        r.conclusao = sb.toString();
        return r;
    }

    /** true = achou, false = não existe, null = estourou o tempo. */
    private Boolean buscarCicloHamiltoniano(int[] caminho, boolean[] usado, int pos, long prazo) {
        int n = numVertices();
        if (System.currentTimeMillis() > prazo) return null;
        int ultimo = caminho[pos - 1];
        if (pos == n) return existeAresta(ultimo, caminho[0]);

        // candidatos ordenados pelo número de vizinhos ainda livres (Warnsdorff)
        List<int[]> candidatos = new ArrayList<>();
        for (int v = 0; v < n; v++) {
            if (usado[v] || !existeAresta(ultimo, v)) continue;
            int livres = 0;
            for (int w = 0; w < n; w++) if (!usado[w] && w != v && existeAresta(v, w)) livres++;
            candidatos.add(new int[]{livres, v});
        }
        candidatos.sort((a, b) -> a[0] != b[0] ? a[0] - b[0] : a[1] - b[1]);

        for (int[] c : candidatos) {
            int v = c[1];
            caminho[pos] = v;
            usado[v] = true;
            if (!algumVerticeSemSaida(caminho[0], v, usado)) {
                Boolean res = buscarCicloHamiltoniano(caminho, usado, pos + 1, prazo);
                if (res == null || res) return res;
            }
            usado[v] = false;
        }
        return false;
    }

    /**
     * Poda: cada vértice ainda não visitado precisa de 2 vizinhos "disponíveis"
     * (não visitados ou uma das pontas do caminho: início e fim) para poder
     * entrar no ciclo. Se algum tiver menos, este ramo já falhou.
     */
    private boolean algumVerticeSemSaida(int inicio, int fim, boolean[] usado) {
        int n = numVertices();
        for (int w = 0; w < n; w++) {
            if (usado[w]) continue;
            int disponiveis = 0;
            for (int x = 0; x < n && disponiveis < 2; x++) {
                if (existeAresta(w, x) && (!usado[x] || x == inicio || x == fim)) disponiveis++;
            }
            if (disponiveis < 2) return true;
        }
        // Poda de conexidade: os não visitados precisam continuar alcançáveis a
        // partir da ponta do caminho andando só por não visitados.
        boolean[] alcancado = new boolean[n];
        java.util.ArrayDeque<Integer> fila = new java.util.ArrayDeque<>();
        fila.add(fim);
        alcancado[fim] = true;
        while (!fila.isEmpty()) {
            int u = fila.poll();
            for (int x = 0; x < n; x++) {
                if (!alcancado[x] && !usado[x] && existeAresta(u, x)) { alcancado[x] = true; fila.add(x); }
            }
        }
        for (int w = 0; w < n; w++) if (!usado[w] && !alcancado[w]) return true;
        return false;
    }
}
