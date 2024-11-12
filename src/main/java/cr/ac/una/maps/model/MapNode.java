package cr.ac.una.maps.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MapNode {
    private String id; // Un identificador único para el nodo
    private double x, y; // Coordenadas del nodo en el mapa
    private List<MapEdge> edges; // Lista de aristas adyacentes (calles)

    public MapNode(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.edges = new ArrayList<>();
    }

    public void addEdge(MapEdge edge) {
        this.edges.add(edge);
    }

    public double distanceTo(MapNode other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    @Override
    public String toString() {
        return id;
    }
}

