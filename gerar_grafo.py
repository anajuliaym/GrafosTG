"""
Bora? - Grafo de Locais de Interesse da Cidade de São Paulo
Teoria dos Grafos - 6º CC - Universidade Presbiteriana Mackenzie

Integrantes:
    Gabriel Mires Camargo     RA 10436741  10436741@mackenzista.com.br
    Ana Julia Yaguti Matilha  RA 10436655  10436655@mackenzista.com.br
    Jiye Huang                RA 10438990  10438990@mackenzista.com.br

Síntese:
    Lê o mapa "Grafo de Locais.kmz" (exportado do Google My Maps) e gera o
    arquivo grafo.txt no formato do enunciado (tipo 2: não orientado com peso
    na aresta). Cada marcador do mapa vira um vértice de local ("Nome [Categoria]");
    a camada do mapa (região) é só um atributo do local e não vira vértice.
    As arestas são:
      1. proximidade: cada local ligado aos k vizinhos mais próximos (k-NN),
         com peso = distância de Haversine em km;
      2. pontes: arestas extras ligando os agrupamentos geográficos que o k-NN
         deixa separados, escolhidas pela menor distância entre componentes
         (mesma ideia do algoritmo de Kruskal), garantindo que o grafo seja conexo.

Uso (na pasta do projeto, sem dependências externas):
    python3 gerar_grafo.py

Histórico de alterações:
    28/09/2026  Jiye Huang                Versão inicial (Parte 2): leitura do KMZ, k-NN
                                          com k = 3 e escrita do grafo.txt.
    04/11/2026  Ana Julia Yaguti Matilha  Parte 3: camada vazia do My Maps substituída pelo
                                          vértice raiz "Cidade de São Paulo" ligado às regiões;
                                          pontes entre componentes pela menor distância
                                          (Kruskal); saída determinística (arestas ordenadas)
                                          e estatísticas impressas no final.
    18/11/2026  Ana Julia Yaguti Matilha  Parte 3: removidos os vértices de cidade e região e
                                          as arestas de peso 0 (não são lugares nem
                                          deslocamento); k passou de 3 para 4; base
                                          ampliada para 118 locais no KMZ.
"""

import math
import os
import zipfile
import xml.etree.ElementTree as ET

ARQUIVO_KMZ = "Grafo de Locais.kmz"
ARQUIVO_SAIDA = "grafo.txt"
NUM_VIZINHOS = int(os.environ.get("K", 4))  # k do k-NN
NS = {"kml": "http://www.opengis.net/kml/2.2"}


def calcular_distancia(lat1, lon1, lat2, lon2):
    """Distância geodésica em km pela fórmula de Haversine (Sinnott, 1984)."""
    raio_terra = 6371.0
    phi1, phi2 = math.radians(lat1), math.radians(lat2)
    dphi = phi2 - phi1
    dlambda = math.radians(lon2 - lon1)
    a = math.sin(dphi / 2) ** 2 + math.cos(phi1) * math.cos(phi2) * math.sin(dlambda / 2) ** 2
    return round(raio_terra * 2 * math.atan2(math.sqrt(a), math.sqrt(1 - a)), 2)


def carregar_raiz_xml(caminho):
    """Abre o KML de dentro do KMZ (um ZIP) ou um .kml solto."""
    if caminho.lower().endswith(".kmz"):
        with zipfile.ZipFile(caminho, "r") as z:
            kmls = [f for f in z.namelist() if f.lower().endswith(".kml")]
            if not kmls:
                raise FileNotFoundError("Nenhum .kml encontrado dentro do KMZ.")
            return ET.fromstring(z.read(kmls[0]))
    return ET.parse(caminho).getroot()


def texto(no, tag):
    filho = no.find(f"kml:{tag}", NS)
    return filho.text.strip() if filho is not None and filho.text else ""


def ler_locais(raiz):
    """Retorna a lista de regiões (camadas com marcadores) e a lista de locais."""
    regioes, locais = [], []
    for pasta in raiz.findall(".//kml:Folder", NS):
        nome_regiao = texto(pasta, "name")
        marcadores = pasta.findall("kml:Placemark", NS)
        # Camadas vazias (ex.: "Camada sem título", criada automaticamente pelo
        # My Maps) não representam região nenhuma e eram um vértice isolado na Parte 2.
        if not nome_regiao or not marcadores:
            continue
        regioes.append(nome_regiao)
        for m in marcadores:
            nome = texto(m, "name")
            coords = m.find(".//kml:Point/kml:coordinates", NS)
            if not nome or coords is None or not coords.text:
                continue
            descricao = texto(m, "description")
            categoria = "Geral"
            if "Categoria:" in descricao:
                categoria = descricao.split("Categoria:")[1].strip().split("\n")[0].strip()
            lon, lat, *_ = map(float, coords.text.strip().split(","))
            locais.append({"nome": nome, "regiao": nome_regiao,
                           "categoria": categoria, "lat": lat, "lon": lon})
    return sorted(regioes), locais


class ConjuntosDisjuntos:
    """Union-Find usado para saber quais locais já estão na mesma componente."""

    def __init__(self, ids):
        self.pai = {i: i for i in ids}

    def achar(self, x):
        while self.pai[x] != x:
            self.pai[x] = self.pai[self.pai[x]]
            x = self.pai[x]
        return x

    def unir(self, a, b):
        ra, rb = self.achar(a), self.achar(b)
        if ra == rb:
            return False
        self.pai[rb] = ra
        return True


def gerar_grafo():
    if not os.path.exists(ARQUIVO_KMZ):
        print(f"Erro: '{ARQUIVO_KMZ}' não encontrado na pasta atual.")
        return

    regioes, locais = ler_locais(carregar_raiz_xml(ARQUIVO_KMZ))

    # ---- Vértices: um por local, na ordem das camadas do mapa ----------------
    rotulos = []
    for loc in locais:
        loc["id"] = len(rotulos)
        rotulos.append(f"{loc['nome']} [{loc['categoria']}]")

    # Distâncias entre todos os pares de locais (70 x 70, custo desprezível)
    dist = {}
    for a in locais:
        for b in locais:
            if a["id"] < b["id"]:
                dist[(a["id"], b["id"])] = calcular_distancia(a["lat"], a["lon"], b["lat"], b["lon"])

    def d(u, v):
        return dist[(min(u, v), max(u, v))]

    # ---- Arestas de proximidade (k-NN) --------------------------------------
    ids_locais = [loc["id"] for loc in locais]
    proximidade = set()
    for u in ids_locais:
        vizinhos = sorted((d(u, v), v) for v in ids_locais if v != u)
        for peso, v in vizinhos[:NUM_VIZINHOS]:
            proximidade.add((min(u, v), max(u, v), peso))

    # ---- Pontes entre componentes (Kruskal sobre as arestas candidatas) ----
    # Parte-se das componentes formadas pelo k-NN e percorrem-se todos os pares
    # de locais em ordem crescente de distância; um par que liga duas
    # componentes diferentes vira aresta. Termina quando sobra uma componente.
    uf = ConjuntosDisjuntos(ids_locais)
    for u, v, _ in proximidade:
        uf.unir(u, v)
    pontes = []
    for (u, v), peso in sorted(dist.items(), key=lambda item: (item[1], item[0])):
        if uf.unir(u, v):
            pontes.append((u, v, peso))

    arestas = sorted(proximidade) + pontes

    with open(ARQUIVO_SAIDA, "w", encoding="utf-8") as f:
        f.write("2\n")
        f.write(f"{len(rotulos)}\n")
        for i, rotulo in enumerate(rotulos):
            f.write(f'{i} "{rotulo}" 0\n')
        f.write(f"{len(arestas)}\n")
        for u, v, peso in arestas:
            f.write(f"{u} {v} {peso}\n")

    pesos = [p for _, _, p in sorted(proximidade) + pontes]
    print(f"'{ARQUIVO_SAIDA}' gerado: {len(rotulos)} vértices (locais de {len(regioes)} regiões), {len(arestas)} arestas")
    print(f"  proximidade k={NUM_VIZINHOS}: {len(proximidade)} | pontes: {len(pontes)}")
    for u, v, p in pontes:
        print(f"    ponte {u} -- {v} ({p} km): {rotulos[u]} <-> {rotulos[v]}")
    print(f"  peso das arestas entre locais: mín {min(pesos)} km, máx {max(pesos)} km, "
          f"média {sum(pesos) / len(pesos):.2f} km")


if __name__ == "__main__":
    gerar_grafo()
