package cr.ac.una.maps.controller;

import com.sun.javafx.geom.Edge;
import cr.ac.una.maps.model.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;

public class MainController extends Controller implements Initializable {

    private MapGraph grafo; // Grafo del mapa
    private MapNode puntoA, puntoB; // Puntos de inicio y destino
    private Canvas canvas;
    private Canvas canvasRoutes;


    @FXML
    private ImageView imvMap;

    @FXML
    private StackPane stackpaneMap;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        grafo = new MapGraph();

        // Crear el canvas para dibujar nodos
        canvas = new Canvas();
        stackpaneMap.getChildren().add(canvas);  // Añadir el canvas al StackPane

        // Crear un segundo canvas para dibujar rutas/aristas
        canvasRoutes = new Canvas();
        stackpaneMap.getChildren().add(canvasRoutes);  // Añadir el canvasRoutes al StackPane

        // Asegurar que ambos canvas cubran todo el stackpane cuando cambie el tamaño
        stackpaneMap.widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            canvasRoutes.setWidth(newVal.doubleValue()); // Sincronizar el tamaño
            canvas.setHeight(stackpaneMap.getHeight());
            canvasRoutes.setHeight(stackpaneMap.getHeight()); // Sincronizar el tamaño
        });
        stackpaneMap.heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            canvasRoutes.setHeight(newVal.doubleValue()); // Sincronizar el tamaño
            canvas.setWidth(stackpaneMap.getWidth());
            canvasRoutes.setWidth(stackpaneMap.getWidth()); // Sincronizar el tamaño
        });

        // Asegurarse de que los canvas estén al frente
        canvas.toFront();
        canvasRoutes.toFront();

        // Esperar a que el StackPane tenga un tamaño definitivo antes de dibujar las aristas
        stackpaneMap.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            if (newBounds.getWidth() > 0 && newBounds.getHeight() > 0) {
                inicializarGrafo();  // Inicializar nodos y aristas
            }
        });

        // Manejar clics en el mapa
        stackpaneMap.setOnMouseClicked(event -> {
            double x = event.getX();
            double y = event.getY();
            manejarClickEnMapa(x, y);
        });
    }



    private void manejarClickEnMapa(double x, double y) {
        // Convertir el clic a una coordenada de nodo
        MapNode nodoClic = encontrarNodoCercano(x, y);

        if (nodoClic == null) {
            System.out.println("No se encontró un nodo cercano.");
            return;
        }

        if (puntoA == null) {
            // Definir el punto A si no ha sido seleccionado
            puntoA = nodoClic;
            System.out.println("Punto A seleccionado: " + puntoA);
            dibujarNodo(puntoA, Color.RED); // Dibuja en rojo el punto A
        } else if (puntoB == null) {
            // Definir el punto B si punto A ya está definido
            puntoB = nodoClic;
            System.out.println("Punto B seleccionado: " + puntoB);
            dibujarNodo(puntoB, Color.BLUE); // Dibuja en azul el punto B

            // Calcular la ruta más corta entre puntoA y puntoB
            List<MapNode> ruta = grafo.findShortestPath(puntoA.getId(), puntoB.getId());
            if (ruta != null && !ruta.isEmpty()) {
                dibujarRuta(ruta); // Dibujar la ruta en verde
            } else {
                System.out.println("No se encontró una ruta entre los nodos seleccionados.");
            }

            // Restablecer los puntos para una nueva selección si se desea
            puntoA = null;
            puntoB = null;
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
        GraphicsContext gc = canvasRoutes.getGraphicsContext2D(); // Usar el canvas de rutas
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(2);

        if (ruta.isEmpty()) {
            System.out.println("No se encontró una ruta entre los puntos A y B.");
            return;
        }

        for (int i = 0; i < ruta.size() - 1; i++) {
            MapNode inicio = ruta.get(i);
            MapNode fin = ruta.get(i + 1);
            System.out.println("Dibujando línea entre " + inicio.getId() + " y " + fin.getId());
            gc.strokeLine(inicio.getX(), inicio.getY(), fin.getX(), fin.getY());
        }
    }


    private void inicializarGrafo() {
        // Agregar nodos con los valores de coordenadas ajustados

        List<MapEdge> edges = new ArrayList<>();

        grafo.addNode("A", 318, 340);
        grafo.addNode("B", 349, 274);
        grafo.addNode("C", 369, 230);
        grafo.addNode("D", 439, 241);
        grafo.addNode("E", 421, 303);
        grafo.addNode("F", 388, 373);
        grafo.addNode("G", 459, 400);
        grafo.addNode("H", 483, 330);
        grafo.addNode("I", 507, 261);
        grafo.addNode("J", 425, 484);
        grafo.addNode("K", 351, 455);
        grafo.addNode("L", 297, 434);
        grafo.addNode("M", 596, 458);
        grafo.addNode("N", 623, 390);
        grafo.addNode("O", 649, 319);
        grafo.addNode("P", 578, 293);
        grafo.addNode("Q", 555, 360);
        grafo.addNode("R", 484, 505);
        grafo.addNode("S", 607, 223);
        grafo.addNode("T", 530, 191);
        grafo.addNode("U", 297, 576);
        grafo.addNode("V", 223, 384);
        grafo.addNode("W", 249, 406);
        grafo.addNode("X", 267, 318);
        grafo.addNode("Y", 440, 157);
        grafo.addNode("Z", 633, 150);
        grafo.addNode("AA", 675, 248);
        grafo.addNode("BB", 716, 347);

        // Nuevos nodos agregados
        grafo.addNode("NC1", 507, 423);
        grafo.addNode("NC2", 462, 247);
        grafo.addNode("NC3", 486, 176);
        grafo.addNode("NC4", 331, 350);
        grafo.addNode("NC5", 516, 347);
        grafo.addNode("NC6", 487, 417);

        // Lista para almacenar las aristas que serán seleccionadas aleatoriamente

        // Agregar aristas al grafo y almacenarlas en la lista
        edges.add(grafo.addEdge("A", "X", calcularDistancia(318, 340, 267, 318), false));
        edges.add(grafo.addEdge("A", "B", calcularDistancia(318, 340, 349, 274), false));
        edges.add(grafo.addEdge("A", "NC4", calcularDistancia(318, 340, 331, 350), false));
        edges.add(grafo.addEdge("B", "A", calcularDistancia(349, 274, 318, 340), false));
        edges.add(grafo.addEdge("B", "E", calcularDistancia(349, 274, 421, 303), true));
        edges.add(grafo.addEdge("B", "C", calcularDistancia(349, 274, 369, 230), false));
        edges.add(grafo.addEdge("C", "B", calcularDistancia(369, 230, 349, 274), false));
        edges.add(grafo.addEdge("D", "C", calcularDistancia(439, 241, 369, 230), false));
        edges.add(grafo.addEdge("D", "Y", calcularDistancia(439, 241, 440, 157), false));
        edges.add(grafo.addEdge("E", "D", calcularDistancia(421, 303, 439, 241), false));
        edges.add(grafo.addEdge("E", "H", calcularDistancia(421, 303, 483, 330), false));
        edges.add(grafo.addEdge("F", "E", calcularDistancia(388, 373, 421, 303), false));
        edges.add(grafo.addEdge("F", "NC4", calcularDistancia(388, 373, 331, 350), false));
        edges.add(grafo.addEdge("F", "G", calcularDistancia(388, 373, 459, 400), false));
        edges.add(grafo.addEdge("F", "K", calcularDistancia(388, 373, 351, 455), false));
        edges.add(grafo.addEdge("G", "F", calcularDistancia(459, 400, 388, 373), false));
        edges.add(grafo.addEdge("G", "NC6", calcularDistancia(459, 400, 487, 417), false));
        edges.add(grafo.addEdge("G", "J", calcularDistancia(459, 400, 425, 484), false));
        edges.add(grafo.addEdge("H", "G", calcularDistancia(483, 330, 459, 400), false));
        edges.add(grafo.addEdge("H", "NC5", calcularDistancia(483, 330, 516, 347), false));
        edges.add(grafo.addEdge("I", "H", calcularDistancia(507, 261, 483, 330), false));
        edges.add(grafo.addEdge("I", "NC2", calcularDistancia(507, 261, 462, 247), false));
        edges.add(grafo.addEdge("J", "G", calcularDistancia(425, 484, 459, 400), false));
        edges.add(grafo.addEdge("J", "K", calcularDistancia(425, 484, 351, 455), false));
        edges.add(grafo.addEdge("K", "J", calcularDistancia(351, 455, 425, 484), false));
        edges.add(grafo.addEdge("K", "F", calcularDistancia(351, 455, 388, 373), false));
        edges.add(grafo.addEdge("K", "U", calcularDistancia(351, 455, 297, 576), false));
        edges.add(grafo.addEdge("K", "L", calcularDistancia(351, 455, 297, 434), false));
        edges.add(grafo.addEdge("L", "W", calcularDistancia(297, 434, 249, 406), false));
        edges.add(grafo.addEdge("L", "NC4", calcularDistancia(297, 434, 331, 350), false));
        edges.add(grafo.addEdge("L", "K", calcularDistancia(297, 434, 351, 455), false));
        edges.add(grafo.addEdge("M", "NC1", calcularDistancia(596, 458, 507, 423), false));
        edges.add(grafo.addEdge("N", "M", calcularDistancia(623, 390, 596, 458), false));
        edges.add(grafo.addEdge("O", "N", calcularDistancia(649, 319, 623, 390), false));
        edges.add(grafo.addEdge("P", "S", calcularDistancia(578, 293, 607, 223), false));
        edges.add(grafo.addEdge("P", "I", calcularDistancia(578, 293, 507, 261), false));
        edges.add(grafo.addEdge("Q", "P", calcularDistancia(555, 360, 578, 293), false));
        edges.add(grafo.addEdge("Q", "N", calcularDistancia(555, 360, 623, 390), false));
        edges.add(grafo.addEdge("R", "NC1", calcularDistancia(484, 505, 507, 423), false));
        edges.add(grafo.addEdge("S", "T", calcularDistancia(607, 223, 530, 191), false));
        edges.add(grafo.addEdge("S", "Z", calcularDistancia(607, 223, 633, 150), false));
        edges.add(grafo.addEdge("T", "NC3", calcularDistancia(530, 191, 486, 176), false));
        edges.add(grafo.addEdge("T", "I", calcularDistancia(530, 191, 507, 261), false));
        edges.add(grafo.addEdge("U", "K", calcularDistancia(297, 576, 351, 455), false));
        edges.add(grafo.addEdge("V", "W", calcularDistancia(223, 384, 249, 406), false));
        edges.add(grafo.addEdge("V", "X", calcularDistancia(223, 384, 267, 318), false));
        edges.add(grafo.addEdge("W", "V", calcularDistancia(249, 406, 223, 384), false));
        edges.add(grafo.addEdge("W", "L", calcularDistancia(249, 406, 297, 434), false));
        edges.add(grafo.addEdge("X", "A", calcularDistancia(267, 318, 318, 340), false));
        edges.add(grafo.addEdge("X", "V", calcularDistancia(267, 318, 223, 384), false));
        edges.add(grafo.addEdge("Y", "NC3", calcularDistancia(440, 157, 486, 176), false));
        edges.add(grafo.addEdge("AA", "S", calcularDistancia(675, 248, 607, 223), false));
        edges.add(grafo.addEdge("AA", "O", calcularDistancia(675, 248, 649, 319), false));
        edges.add(grafo.addEdge("BB", "O", calcularDistancia(716, 347, 649, 319), false));


        grafo.addEdge("NC1", "NC6", calcularDistancia(507, 423, 487, 417), false);
        grafo.addEdge("NC1", "M", calcularDistancia(507, 423, 596, 458), false);
        //grafo.addEdge("NC1", "R", calcularDistancia(507, 423, 484, 505), false);

        grafo.addEdge("NC2", "D", calcularDistancia(462, 247, 439, 241), false);
        //grafo.addEdge("NC2", "I", calcularDistancia(462, 247, 507, 261), false);
        grafo.addEdge("NC2", "NC3", calcularDistancia(462, 247, 486, 176), false);

        //grafo.addEdge("NC3", "Y", calcularDistancia(486, 176, 440, 157), false);
        grafo.addEdge("NC3", "NC2", calcularDistancia(486, 176, 462, 247), false);
        grafo.addEdge("NC3", "T", calcularDistancia(486, 176, 530, 191), false);

        grafo.addEdge("NC4", "A", calcularDistancia(331, 350, 318, 340), false);
        grafo.addEdge("NC4", "L", calcularDistancia(331, 350, 297, 434), false);
        grafo.addEdge("NC4", "F", calcularDistancia(331, 350, 388, 373), false);

        //grafo.addEdge("NC5", "H", calcularDistancia(516, 347, 483, 330), false);
        //grafo.addEdge("NC5", "NC6", calcularDistancia(516, 347, 487, 417), false);
        grafo.addEdge("NC5", "Q", calcularDistancia(516, 347, 555, 360), false);

        grafo.addEdge("NC6", "NC5", calcularDistancia(487, 417, 516, 347), false);
        grafo.addEdge("NC6", "G", calcularDistancia(487, 417, 459, 400), false);
        grafo.addEdge("NC6", "NC1", calcularDistancia(487, 417, 507, 423), false);

        Random rand = new Random();
        Set<MapEdge> processedEdges = new HashSet<>();

        // Seleccionar aristas aleatorias y asignar tráfico pesado
        for (int i = 0; i < 5; i++) {
            MapEdge selectedEdge;
            do {
                int randomIndex = rand.nextInt(edges.size());
                selectedEdge = edges.get(randomIndex);
            } while (processedEdges.contains(selectedEdge));

            // Asignar tráfico pesado aumentando el peso
            selectedEdge.setWeight(selectedEdge.getWeight() * 2);
            System.out.println("Tráfico pesado asignado a la arista: " + selectedEdge);

            // Añadir la arista procesada al set para evitar duplicados
            processedEdges.add(selectedEdge);
        }

        // Dibujar las aristas con tráfico pesado en amarillo
        for (MapEdge edge : processedEdges) {
            dibujarArista(edge, Color.YELLOW); // Asegurar que las aristas con tráfico se dibujen en amarillo
        }

        canvas.setWidth(canvas.getWidth() + 1);
        canvas.setWidth(canvas.getWidth() - 1);

        for (MapEdge edge : grafo.getEdges()) {
            if (edge.getWeight() > 1) { // Tráfico pesado
                dibujarArista(edge, Color.YELLOW);
            } else {
                dibujarArista(edge, Color.BLACK);
            }
        }
    }

    private void dibujarArista(MapEdge edge, Color color) {
        if (canvasRoutes != null) {
            GraphicsContext gc = canvasRoutes.getGraphicsContext2D(); // Usar el canvas de rutas
            gc.setStroke(color);
            gc.setLineWidth(3); // Establecer grosor de la línea

            // Dibuja la línea conectando dos nodos
            double x1 = edge.getFrom().getX();
            double y1 = edge.getFrom().getY();
            double x2 = edge.getTo().getX();
            double y2 = edge.getTo().getY();

            System.out.println("Dibujando arista desde " + edge.getFrom().getId() + " hasta " + edge.getTo().getId() + " en color " + color.toString());
            gc.strokeLine(x1, y1, x2, y2); // Dibuja la línea entre los nodos
        } else {
            System.err.println("Canvas no ha sido inicializado. No se puede dibujar.");
        }
    }

    // Método para calcular la distancia entre dos puntos en el mapa (nodos)
    private double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }


    @Override
    public void initialize() {
        // Método de inicialización adicional
    }
}
