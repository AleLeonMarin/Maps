package cr.ac.una.maps.model;

import lombok.Data;

import java.util.*;

@Data
public class MapGraph {
    private Map<String, MapNode> nodes; // Mapa de nodos por su ID
    private List<MapEdge> edges; // Lista de todas las aristas
    private int[][] next; // Matriz de predecesores

    public MapGraph() {
        nodes = new HashMap<>();
        edges = new ArrayList<>();
    }

    public void addNode(String id, double x, double y) {
        nodes.put(id, new MapNode(id, x, y));
    }

    public MapEdge addEdge(String fromId, String toId, double weight, boolean hasAccident) {
        MapNode from = nodes.get(fromId);
        MapNode to = nodes.get(toId);
        if (from != null && to != null) {
            MapEdge edge = new MapEdge(from, to, weight, hasAccident); // Pasar el parámetro hasAccident
            from.addEdge(edge);
            edges.add(edge);
            return edge; // Devolver la arista creada
        }
        return null; // Si no se pudo crear la arista
    }

    public MapNode getNode(String id) {
        return nodes.get(id);
    }

    public List<MapNode> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public void aplicarTransitoPesadoAleatorio() {
        Random rand = new Random();
        List<MapEdge> aristasSeleccionadas = new ArrayList<>(edges);

        if (aristasSeleccionadas.size() >= 5) {
            Collections.shuffle(aristasSeleccionadas); // Aleatorizar la lista
            for (int i = 0; i < 5; i++) {
                MapEdge edge = aristasSeleccionadas.get(i);
                edge.setWeight(edge.getWeight() * 2); // Duplicar el peso por tráfico pesado
                System.out.println("Tráfico pesado aplicado en la arista: " + edge);
            }
        } else {
            System.out.println("No hay suficientes aristas para aplicar tráfico pesado.");
        }
    }

    public List<MapNode> findShortestPath(String inicio, String fin) {
        Map<MapNode, MapNode> predecesores = new HashMap<>();
        Map<MapNode, Double> distancias = new HashMap<>();
        PriorityQueue<MapNode> cola = new PriorityQueue<>(Comparator.comparing(distancias::get));

        for (MapNode nodo : this.getNodes()) {
            distancias.put(nodo, Double.MAX_VALUE);
        }
        MapNode nodoInicio = getNode(inicio);
        distancias.put(nodoInicio, 0.0);
        cola.add(nodoInicio);

        while (!cola.isEmpty()) {
            MapNode actual = cola.poll();
            if (actual.getId().equals(fin)) break;

            for (MapEdge arista : actual.getEdges()) {
                if (!arista.isClosed() && (!arista.isHasAccident() || arista.getFrom().equals(actual))) {
                    MapNode vecino = arista.getTo();
                    double nuevaDistancia = distancias.get(actual) + arista.getWeight();
                    if (nuevaDistancia < distancias.get(vecino)) {
                        distancias.put(vecino, nuevaDistancia);
                        predecesores.put(vecino, actual);
                        cola.add(vecino);
                    }
                }
            }
        }

        List<MapNode> ruta = new ArrayList<>();
        MapNode actual = getNode(fin);
        while (actual != null) {
            ruta.add(0, actual); // Añadir el nodo al inicio de la lista
            actual = predecesores.get(actual);
        }

        return ruta;
    }

    public double[][] floydWarshall() {
        int cantidadNodos = nodes.size();
        double[][] distance = new double[cantidadNodos][cantidadNodos];
        next = new int[cantidadNodos][cantidadNodos]; // Matriz de predecesores
        MapNode[] nodeArray = nodes.values().toArray(new MapNode[0]);

        // Inicializar distancias y predecesores
        for (int i = 0; i < cantidadNodos; i++) {
            for (int j = 0; j < cantidadNodos; j++) {
                if (i == j) {
                    distance[i][j] = 0;
                } else {
                    distance[i][j] = Double.MAX_VALUE;
                }
                next[i][j] = -1; // Inicialmente, no hay predecesores
            }
        }

        // Establecer distancias iniciales respetando aristas unidireccionales y cerradas
        for (MapEdge edge : edges) {
            if (!edge.isClosed()) { // Ignorar aristas cerradas
                int fromIndex = Arrays.asList(nodeArray).indexOf(edge.getFrom());
                int toIndex = Arrays.asList(nodeArray).indexOf(edge.getTo());

                // Configurar la distancia de `from` a `to`
                distance[fromIndex][toIndex] = edge.getWeight();
                next[fromIndex][toIndex] = toIndex;

                // Solo configurar la distancia de regreso `to` a `from` si la arista no es unidireccional
                if (edge.isHasAccident()) {
                    distance[toIndex][fromIndex] = edge.getWeight();
                    next[toIndex][fromIndex] = fromIndex;
                }
            }
        }

        // Algoritmo de Floyd-Warshall con manejo de predecesores
        for (int k = 0; k < cantidadNodos; k++) {
            for (int i = 0; i < cantidadNodos; i++) {
                for (int j = 0; j < cantidadNodos; j++) {
                    // Verificar que i -> k y k -> j tengan caminos válidos antes de actualizar
                    if (distance[i][k] != Double.MAX_VALUE && distance[k][j] != Double.MAX_VALUE) {
                        double newDist = distance[i][k] + distance[k][j];
                        if (newDist < distance[i][j]) {
                            distance[i][j] = newDist;
                            next[i][j] = next[i][k]; // Actualizar el predecesor
                        }
                    }
                }
            }
        }

        return distance;
    }


    public int[][] getNextMatrix() {
        return next;
    }

    public List<MapNode> getPathFromFloydWarshall(int start, int end, int[][] next) {
        List<MapNode> path = new ArrayList<>();
        if (next[start][end] == -1) return path; // No hay camino

        path.add(nodes.values().toArray(new MapNode[0])[start]);
        while (start != end) {
            start = next[start][end];
            path.add(nodes.values().toArray(new MapNode[0])[start]);
        }
        return path;
    }

    public MapEdge getEdge(MapNode from, MapNode to) {
        for (MapEdge edge : edges) {
            if ((edge.getFrom().equals(from) && edge.getTo().equals(to)) ||
                    (edge.getFrom().equals(to) && edge.getTo().equals(from))) {
                return edge;
            }
        }
        return null; // Retorna null si no se encuentra una arista entre los nodos
    }

}