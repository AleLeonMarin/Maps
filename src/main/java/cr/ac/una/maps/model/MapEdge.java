package cr.ac.una.maps.model;

import lombok.Data;

@Data
public class MapEdge {
    private MapNode from;
    private MapNode to;
    private double weight; // Peso de la arista (puede ser distancia, tiempo, etc.)
    private boolean closed; // Estado de la calle (cerrada o abierta)
    private boolean hasAccident; // Si la calle es unidireccional o no

    public MapEdge(MapNode from, MapNode to, double weight, boolean hasAccident) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.closed = false;  // Por defecto, la calle está abierta
        this.hasAccident = hasAccident; // Define si la calle es unidireccional
    }

    public double getWeight() {
        return closed ? Double.MAX_VALUE : weight; // Si la calle está cerrada, asignar peso infinito
    }


    @Override
    public String toString() {
        return from + " -> " + to + " (Weight: " + weight + ", OneWay: " + hasAccident + ", Closed: " + closed + ")";
    }
}

