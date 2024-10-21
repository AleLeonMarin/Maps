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

    public MapEdge addEdge(String fromId, String toId, double weight, boolean oneWay) {
        MapNode from = nodes.get(fromId);
        MapNode to = nodes.get(toId);
        if (from != null && to != null) {
            MapEdge edge = new MapEdge(from, to, weight, oneWay); // Pasar el parámetro oneWay
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

    public List<MapEdge> getEdges() {
        return edges;
    }

    // Método para aplicar tráfico pesado aleatoriamente en 5 aristas
    public void aplicarTransitoPesadoAleatorio() {
        Random rand = new Random();
        List<MapEdge> aristasSeleccionadas = new ArrayList<>(edges);

        // Verificar que haya al menos 5 aristas
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

            // Recorremos los vecinos (solo si es una calle válida)
            for (MapEdge arista : actual.getEdges()) {
                if (!arista.isClosed() && (!arista.isOneWay() || arista.getFrom().equals(actual))) {
                    // Solo considerar calles abiertas y respetar el sentido unidireccional
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

        // Reconstruir la ruta desde el nodo final
        List<MapNode> ruta = new ArrayList<>();
        MapNode actual = getNode(fin);
        while (actual != null) {
            ruta.add(0, actual); // Añadir el nodo al inicio de la lista
            actual = predecesores.get(actual);
        }

        return ruta;
    }
}




