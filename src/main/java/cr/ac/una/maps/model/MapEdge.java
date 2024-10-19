package cr.ac.una.maps.model;

import java.util.ArrayList;
import java.util.List;

public class MapEdge {
    private MapNode from;
    private MapNode to;
    private double weight; // Peso de la arista (puede ser distancia, tiempo, etc.)
    boolean closed; // Estado de la calle (cerrada o abierta)

    public MapEdge(MapNode from, MapNode to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.closed = false;
    }

    public MapNode getFrom() {
        return from;
    }

    public MapNode getTo() {
        return to;
    }

    public double getWeight() {
        return closed ? Double.MAX_VALUE : weight; // Si está cerrada, peso infinito
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public String toString() {
        return from + " -> " + to + " (Weight: " + weight + ")";
    }
}
