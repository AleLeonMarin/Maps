package cr.ac.una.maps.model;

public class MapEdge {
    private MapNode from;
    private MapNode to;
    private double weight; // Peso de la arista (puede ser distancia, tiempo, etc.)
    private boolean closed; // Estado de la calle (cerrada o abierta)
    private boolean oneWay; // Si la calle es unidireccional o no

    public MapEdge(MapNode from, MapNode to, double weight, boolean oneWay) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.closed = false;  // Por defecto, la calle está abierta
        this.oneWay = oneWay; // Define si la calle es unidireccional
    }

    public MapNode getFrom() {
        return from;
    }

    public MapNode getTo() {
        return to;
    }

    public double getWeight() {
        return closed ? Double.MAX_VALUE : weight; // Si la calle está cerrada, asignar peso infinito
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isOneWay() {
        return oneWay;
    }

    @Override
    public String toString() {
        return from + " -> " + to + " (Weight: " + weight + ", OneWay: " + oneWay + ", Closed: " + closed + ")";
    }
}

