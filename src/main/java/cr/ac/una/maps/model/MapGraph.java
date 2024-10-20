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

    // Método en la clase MapGraph para encontrar la ruta más corta usando Dijkstra
    public List<MapNode> findShortestPath(String inicio, String fin) {
        Map<MapNode, MapNode> predecesores = new HashMap<>();
        Map<MapNode, Double> distancias = new HashMap<>();
        PriorityQueue<MapNode> cola = new PriorityQueue<>(Comparator.comparing(distancias::get));

        // Inicialización
        for (MapNode nodo : this.getNodes()) {
            distancias.put(nodo, Double.MAX_VALUE);
        }
        MapNode nodoInicio = getNode(inicio);
        distancias.put(nodoInicio, 0.0);
        cola.add(nodoInicio);

        while (!cola.isEmpty()) {
            MapNode actual = cola.poll();
            if (actual.getId().equals(fin)) break;

            // Recorremos los vecinos
            for (MapEdge arista : actual.getEdges()) {
                MapNode vecino = arista.getTo();  // Cambiado a getTo()
                double nuevaDistancia = distancias.get(actual) + arista.getWeight();  // Cambiado a getWeight()
                if (nuevaDistancia < distancias.get(vecino)) {
                    distancias.put(vecino, nuevaDistancia);
                    predecesores.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        // Reconstruir la ruta desde el nodo final
        List<MapNode> ruta = new ArrayList<>();
        MapNode actual = getNode(fin);
        while (actual != null) {
            ruta.add(0, actual); // Añadimos al inicio
            actual = predecesores.get(actual);
        }

        return ruta;
    }


}

