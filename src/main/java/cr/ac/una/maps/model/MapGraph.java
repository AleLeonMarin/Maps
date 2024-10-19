package cr.ac.una.maps.model;

import java.util.ArrayList;
import java.util.List;

import java.util.*;

public class MapGraph {
    private Map<String, MapNode> nodes; // Mapa de nodos por su ID
    private List<MapEdge> edges; // Lista de todas las aristas

    public MapGraph() {
        nodes = new HashMap<>();
        edges = new ArrayList<>();
    }

    public void addNode(String id, double x, double y) {
        nodes.put(id, new MapNode(id, x, y));
    }

    public void addEdge(String fromId, String toId, double weight) {
        MapNode from = nodes.get(fromId);
        MapNode to = nodes.get(toId);
        if (from != null && to != null) {
            MapEdge edge = new MapEdge(from, to, weight);
            from.addEdge(edge);
            edges.add(edge);
        }
    }

    public MapNode getNode(String id) {
        return nodes.get(id);
    }

    public List<MapNode> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public List<MapEdge> getEdges() {
        return edges;
    }

    // Método para encontrar la ruta más corta usando Dijkstra
    public List<MapNode> findShortestPath(String startId, String endId) {
        MapNode start = nodes.get(startId);
        MapNode end = nodes.get(endId);

        if (start == null || end == null) {
            return null; // Si alguno de los nodos no existe
        }

        // Implementación de Dijkstra
        Map<MapNode, Double> distancias = new HashMap<>();
        Map<MapNode, MapNode> predecesores = new HashMap<>();
        PriorityQueue<MapNode> cola = new PriorityQueue<>(Comparator.comparingDouble(distancias::get));

        for (MapNode nodo : nodes.values()) {
            distancias.put(nodo, Double.MAX_VALUE);
        }
        distancias.put(start, 0.0);
        cola.add(start);

        while (!cola.isEmpty()) {
            MapNode actual = cola.poll();
            if (actual.equals(end)) {
                break;
            }

            for (MapEdge arista : actual.getEdges()) {
                MapNode vecino = arista.getTo();
                if (!arista.closed) {
                    double nuevaDistancia = distancias.get(actual) + arista.getWeight();
                    if (nuevaDistancia < distancias.get(vecino)) {
                        distancias.put(vecino, nuevaDistancia);
                        predecesores.put(vecino, actual);
                        cola.add(vecino);
                    }
                }
            }
        }

        // Reconstruir el camino
        List<MapNode> camino = new ArrayList<>();
        for (MapNode nodo = end; nodo != null; nodo = predecesores.get(nodo)) {
            camino.add(nodo);
        }
        Collections.reverse(camino);
        return camino;
    }
}

