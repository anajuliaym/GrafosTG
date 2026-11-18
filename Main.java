import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

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
 *   Ponto de entrada da aplicação: mostra o título e o menu de opções (a até m),
 *   lê as entradas do usuário, trata valores inválidos e chama as operações da
 *   classe Grafo.
 *
 * Histórico de alterações:
 *   28/09/2026  Ana Julia Yaguti Matilha  Versão inicial (Parte 2): menu de opções a até j.
 *   04/11/2026  Ana Julia Yaguti Matilha  Parte 3: opções j (locais próximos por raio
 *                                         e categoria) e k (menor caminho entre dois
 *                                         locais); local escolhido por id ou nome.
 *   18/11/2026  Ana Julia Yaguti Matilha  Parte 3: opção l (graus, coloração, Euler e
 *                                         Hamilton); encerrar passou para a opção m.
 */
public class Main {

    private static final String TITULO =
            "===== Bora? Recomendação de Locais Próximos em São Paulo (Grafo de Locais) =====";
    private static final String ARQUIVO_PADRAO = "grafo.txt";

    private static Grafo grafo = new Grafo();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws UnsupportedEncodingException {
        // Força UTF-8 na entrada/saída, independente do encoding padrão do console
        // (evita caracteres acentuados quebrados no Windows/VS Code).
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        scanner.useLocale(Locale.US); // garante "." como separador decimal na leitura de doubles

        boolean sair = false;
        while (!sair) {
            exibirMenu();
            String opcao = scanner.nextLine().trim().toLowerCase();

            switch (opcao) {
                case "a": lerArquivo(); break;
                case "b": gravarArquivo(); break;
                case "c": inserirVertice(); break;
                case "d": inserirAresta(); break;
                case "e": removerVertice(); break;
                case "f": removerAresta(); break;
                case "g": mostrarConteudoArquivo(); break;
                case "h": mostrarGrafo(); break;
                case "i": apresentarConexidade(); break;
                case "j": buscarLocaisProximos(); break;
                case "k": menorCaminho(); break;
                case "l": caracteristicas(); break;
                case "m": sair = true; System.out.println("Encerrando a aplicação. Até logo!"); break;
                default: System.out.println("Opção inválida. Tente novamente.\n");
            }
        }
        scanner.close();
    }

    private static void exibirMenu() {
        System.out.println("\n" + TITULO);
        System.out.println("Vértices carregados: " + grafo.numVertices()
                + " | Arestas carregadas: " + grafo.numArestas()
                + " | Tipo: " + grafo.getTipo() + " (" + Grafo.descreverTipo(grafo.getTipo()) + ")");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("a) Ler dados do arquivo grafo.txt");
        System.out.println("b) Gravar dados no arquivo grafo.txt");
        System.out.println("c) Inserir vértice");
        System.out.println("d) Inserir aresta");
        System.out.println("e) Remover vértice");
        System.out.println("f) Remover aresta");
        System.out.println("g) Mostrar conteúdo do arquivo");
        System.out.println("h) Mostrar grafo");
        System.out.println("i) Apresentar a conexidade do grafo e o reduzido");
        System.out.println("j) Buscar locais próximos a um local (raio e categoria) - Dijkstra");
        System.out.println("k) Menor caminho entre dois locais - Dijkstra");
        System.out.println("l) Características do grafo (graus, coloração, Euler e Hamilton)");
        System.out.println("m) Encerrar a aplicação");
        System.out.print("Escolha uma opção: ");
    }

    // a) ----------------------------------------------------------------
    private static void lerArquivo() {
        System.out.print("Caminho do arquivo (ENTER para usar '" + ARQUIVO_PADRAO + "'): ");
        String caminho = scanner.nextLine().trim();
        if (caminho.isEmpty()) caminho = ARQUIVO_PADRAO;
        try {
            grafo.lerArquivo(caminho);
            System.out.println("Arquivo lido com sucesso! " + grafo.numVertices()
                    + " vértices e " + grafo.numArestas() + " arestas carregados.");
        } catch (IOException | NumberFormatException e) {
            System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        }
    }

    // b) ----------------------------------------------------------------
    private static void gravarArquivo() {
        System.out.print("Caminho do arquivo (ENTER para usar '" + ARQUIVO_PADRAO + "'): ");
        String caminho = scanner.nextLine().trim();
        if (caminho.isEmpty()) caminho = ARQUIVO_PADRAO;
        try {
            grafo.gravarArquivo(caminho);
            System.out.println("Arquivo '" + caminho + "' gravado com sucesso!");
        } catch (IOException e) {
            System.out.println("Erro ao gravar o arquivo: " + e.getMessage());
        }
    }

    // c) ----------------------------------------------------------------
    private static void inserirVertice() {
        System.out.print("Endereço/nome do local (ex.: 'Museu do Ipiranga'): ");
        String endereco = scanner.nextLine().trim();
        System.out.print("Categoria (ENTER para deixar sem categoria, ex.: 'Cultura'): ");
        String categoria = scanner.nextLine().trim();

        String rotulo = categoria.isEmpty() ? endereco : endereco + " [" + categoria + "]";

        double peso = 0.0;
        if (grafo.temPesoVertice()) {
            System.out.print("Peso do vértice: ");
            peso = lerDouble();
        }
        int id = grafo.inserirVertice(endereco, rotulo, peso);
        System.out.println("Vértice inserido com id " + id + " (rótulo: \"" + rotulo + "\").");
    }

    // d) ----------------------------------------------------------------
    private static void inserirAresta() {
        if (grafo.numVertices() == 0) {
            System.out.println("Não há vértices no grafo ainda.");
            return;
        }
        System.out.print("Id do vértice de origem: ");
        int origem = lerInt();
        System.out.print("Id do vértice de destino: ");
        int destino = lerInt();
        double peso = 0.0;
        if (grafo.temPesoAresta()) {
            System.out.print("Peso da aresta: ");
            peso = lerDouble();
        }
        boolean ok = grafo.inserirAresta(origem, destino, peso);
        System.out.println(ok ? "Aresta inserida com sucesso!" : "Não foi possível inserir a aresta (ids inválidos).");
    }

    // e) ----------------------------------------------------------------
    private static void removerVertice() {
        System.out.print("Id do vértice a remover: ");
        int id = lerInt();
        boolean ok = grafo.removerVertice(id);
        System.out.println(ok ? "Vértice removido (e arestas associadas)!" : "Id de vértice inválido.");
    }

    // f) ----------------------------------------------------------------
    private static void removerAresta() {
        System.out.print("Id do vértice de origem: ");
        int origem = lerInt();
        System.out.print("Id do vértice de destino: ");
        int destino = lerInt();
        boolean ok = grafo.removerAresta(origem, destino);
        System.out.println(ok ? "Aresta removida com sucesso!" : "Aresta não encontrada ou ids inválidos.");
    }

    // g) ----------------------------------------------------------------
    private static void mostrarConteudoArquivo() {
        System.out.println(grafo.mostrarConteudoArquivo());
    }

    // h) ----------------------------------------------------------------
    private static void mostrarGrafo() {
        if (grafo.numVertices() > 25) {
            System.out.print("Grafo tem " + grafo.numVertices()
                    + " vértices. Mostrar como (1) lista de adjacência ou (2) matriz? ");
            String opc = scanner.nextLine().trim();
            if (opc.equals("2")) {
                System.out.println(grafo.mostrarMatriz());
                return;
            }
        }
        System.out.println(grafo.mostrarGrafo());
    }

    // i) ----------------------------------------------------------------
    private static void apresentarConexidade() {
        Grafo.ResultadoConexidade r = grafo.apresentarConexidade();
        System.out.println("Classificação: " + r.classificacao);
        System.out.println("Quantidade de componentes: " + r.componentes.size());
        for (int i = 0; i < r.componentes.size(); i++) {
            System.out.println("  Componente " + i + ": " + r.componentes.get(i));
        }
        if (r.grafoReduzidoTexto != null) {
            System.out.println("\n--- Grafo Reduzido ---");
            System.out.println(r.grafoReduzidoTexto);
        }
    }

    // j) ----------------------------------------------------------------
    private static void buscarLocaisProximos() {
        if (!grafoCarregado()) return;
        int origem = lerLocal("Local de origem (id ou parte do nome): ");
        if (origem < 0) return;
        System.out.print("Raio em km (1, 3, 5 ou outro valor; ENTER = 1): ");
        String txtRaio = scanner.nextLine().trim().replace(",", ".");
        double raio = 1.0;
        if (!txtRaio.isEmpty()) {
            try {
                raio = Double.parseDouble(txtRaio);
            } catch (NumberFormatException e) {
                System.out.println("Raio inválido, usando 1 km.");
            }
        }
        if (raio <= 0) {
            System.out.println("O raio precisa ser positivo.");
            return;
        }
        System.out.println("Categorias: " + String.join(", ", grafo.categorias()));
        System.out.print("Categoria (ENTER = todas): ");
        String categoria = scanner.nextLine().trim();
        if (!categoria.isEmpty() && grafo.categorias().stream().noneMatch(c -> c.equalsIgnoreCase(categoria))) {
            System.out.println("Categoria inexistente.");
            return;
        }

        List<Grafo.LocalProximo> lista = grafo.buscarProximos(origem, raio, categoria);
        System.out.printf("%nLocais a até %s km de \"%s\"%s, pela rede de proximidade:%n",
                Endereco.formatarPeso(raio), grafo.rotulo(origem),
                categoria.isEmpty() ? "" : " na categoria " + categoria);
        if (lista.isEmpty()) {
            System.out.println("  Nenhum local encontrado. Tente aumentar o raio ou mudar a categoria.");
            return;
        }
        for (Grafo.LocalProximo p : lista) {
            System.out.printf("  %5.2f km  [%2d] %-55s (%d trecho%s)%n", p.distanciaKm, p.id,
                    grafo.rotulo(p.id), p.saltos, p.saltos == 1 ? "" : "s");
        }
        System.out.println("Total: " + lista.size() + " local(is).");
    }

    // k) ----------------------------------------------------------------
    private static void menorCaminho() {
        if (!grafoCarregado()) return;
        int origem = lerLocal("Local de origem (id ou parte do nome): ");
        if (origem < 0) return;
        int destino = lerLocal("Local de destino (id ou parte do nome): ");
        if (destino < 0) return;

        Grafo.ResultadoDijkstra r = grafo.dijkstra(origem);
        List<Integer> caminho = r.caminhoAte(destino);
        if (caminho.isEmpty()) {
            System.out.println("Não há caminho entre os dois locais pela rede de proximidade.");
            return;
        }
        System.out.printf("%nMenor caminho (%.2f km, %d trecho(s)):%n", r.distancia[destino], caminho.size() - 1);
        for (int i = 0; i < caminho.size(); i++) {
            int v = caminho.get(i);
            String trecho = (i == 0) ? "" : String.format("  (+%.2f km, acumulado %.2f km)",
                    r.distancia[v] - r.distancia[caminho.get(i - 1)], r.distancia[v]);
            System.out.printf("  %d. [%2d] %s%s%n", i + 1, v, grafo.rotulo(v), trecho);
        }
    }

    // l) ----------------------------------------------------------------
    private static void caracteristicas() {
        if (!grafoCarregado()) return;
        System.out.println("Qual característica do grafo?");
        System.out.println("  1) Grau dos vértices");
        System.out.println("  2) Coloração e número cromático");
        System.out.println("  3) Grafo euleriano (caminho euleriano)");
        System.out.println("  4) Grafo hamiltoniano (ciclo hamiltoniano)");
        System.out.print("Escolha (1 a 4): ");
        String opc = scanner.nextLine().trim();
        switch (opc) {
            case "1":
                System.out.println("\n=== Grau dos vértices ===");
                System.out.print(grafo.relatorioGraus());
                break;
            case "2":
                System.out.println("\n=== Coloração de vértices e número cromático ===");
                System.out.print(grafo.relatorioColoracao());
                break;
            case "3":
                System.out.println("\n=== Grafo euleriano ===");
                System.out.print(grafo.relatorioEuler());
                break;
            case "4":
                System.out.println("\n=== Grafo hamiltoniano ===");
                Grafo.ResultadoHamilton h = grafo.verificarHamilton(5000);
                System.out.println(h.conclusao);
                if (h.ciclo != null) {
                    System.out.println("Ciclo: " + h.ciclo.toString()
                            .replace(", ", " -> ").replace("[", "").replace("]", ""));
                }
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    // ---------------------------------------------------------------------
    private static boolean grafoCarregado() {
        if (grafo.numVertices() == 0) {
            System.out.println("Grafo vazio. Use a opção a) para ler o grafo.txt primeiro.");
            return false;
        }
        return true;
    }

    /**
     * Lê um vértice de LOCAL pelo id ou por um trecho do nome. Se o trecho
     * casar com vários locais, lista as opções e pede o id. Retorna -1 se
     * a entrada não identificar um local.
     */
    private static int lerLocal(String mensagem) {
        System.out.print(mensagem);
        String entrada = scanner.nextLine().trim();
        if (entrada.isEmpty()) {
            System.out.println("Nada informado.");
            return -1;
        }
        int id;
        if (entrada.matches("\\d+")) {
            id = Integer.parseInt(entrada);
        } else {
            List<Integer> achados = new ArrayList<>();
            achados.addAll(grafo.procurarPorNome(entrada));
            if (achados.isEmpty()) {
                System.out.println("Nenhum local contém \"" + entrada + "\".");
                return -1;
            }
            if (achados.size() > 1) {
                System.out.println("Vários locais encontrados:");
                for (int v : achados) System.out.printf("  [%2d] %s%n", v, grafo.rotulo(v));
                System.out.print("Digite o id: ");
                id = lerInt();
            } else {
                id = achados.get(0);
            }
        }
        if (!grafo.isVertice(id)) {
            System.out.println("O id " + id + " não corresponde a nenhum local do grafo.");
            return -1;
        }
        System.out.println("  -> " + grafo.rotulo(id));
        return id;
    }

    private static int lerInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("Valor inválido, digite um número inteiro: ");
            }
        }
    }

    private static double lerDouble() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.print("Valor inválido, digite um número (use ponto para decimais): ");
            }
        }
    }
}
