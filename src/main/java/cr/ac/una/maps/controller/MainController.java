package cr.ac.una.maps.controller;

import cr.ac.una.maps.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController extends Controller implements Initializable {

    private MapGraph grafo; // Grafo del mapa
    private MapNode puntoA, puntoB; // Puntos de inicio y destino
    private Canvas canvas;

    @FXML
    private ImageView imvMap;

    @FXML
    private StackPane stackpaneMap;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Inicializar el grafo y el mapa
        grafo = new MapGraph();
        inicializarGrafo(); // Método para definir nodos y aristas

        // Crear el canvas para dibujar
        canvas = new Canvas(stackpaneMap.getWidth(), stackpaneMap.getHeight());
        stackpaneMap.getChildren().add(canvas);

        // Manejar clics en el mapa
        stackpaneMap.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();
            manejarClickEnMapa(x, y);
        });

        // Asegurar que el canvas cubra todo el stackpane
        stackpaneMap.widthProperty().addListener((obs, oldVal, newVal) -> canvas.setWidth((double) newVal));
        stackpaneMap.heightProperty().addListener((obs, oldVal, newVal) -> canvas.setHeight((double) newVal));
    }

    private void manejarClickEnMapa(double x, double y) {
        // Convertir el clic a una coordenada de nodo
        MapNode nodoClic = encontrarNodoCercano(x, y);

        if (puntoA == null) {
            puntoA = nodoClic;
            System.out.println("Punto A definido: " + puntoA);
            dibujarNodo(puntoA, Color.RED); // Dibuja en rojo el punto A
        } else if (puntoB == null) {
            puntoB = nodoClic;
            System.out.println("Punto B definido: " + puntoB);
            dibujarNodo(puntoB, Color.BLUE); // Dibuja en azul el punto B

            // Calcular la ruta más corta
            List<MapNode> ruta = grafo.findShortestPath(puntoA.getId(), puntoB.getId());
            dibujarRuta(ruta);
        }
    }

    private MapNode encontrarNodoCercano(double x, double y) {
        // Buscar el nodo más cercano basado en las coordenadas
        MapNode nodoCercano = null;
        double distanciaMinima = Double.MAX_VALUE;

        for (MapNode nodo : grafo.getNodes()) {
            double distancia = nodo.distanceTo(new MapNode("", x, y));
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                nodoCercano = nodo;
            }
        }

        return nodoCercano;
    }

    private void dibujarNodo(MapNode nodo, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(color);
        gc.fillOval(nodo.getX() - 5, nodo.getY() - 5, 10, 10); // Dibuja un círculo
    }

    private void dibujarRuta(List<MapNode> ruta) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(2);

        for (int i = 0; i < ruta.size() - 1; i++) {
            MapNode inicio = ruta.get(i);
            MapNode fin = ruta.get(i + 1);
            gc.strokeLine(inicio.getX(), inicio.getY(), fin.getX(), fin.getY());
        }
    }

    // Método para inicializar el grafo (nodos y aristas)
    private void inicializarGrafo() {
        // Agregar nodos y aristas según el mapa
        grafo.addNode("A", 100, 150);
        grafo.addNode("B", 200, 250);
        grafo.addNode("C", 300, 350);

        grafo.addEdge("A", "B", 10);
        grafo.addEdge("B", "C", 15);
        grafo.addEdge("A", "C", 20);
    }

    @Override
    public void initialize() {
        // Método de inicialización adicional
    }
}
