package cr.ac.una.maps.controller;

import cr.ac.una.maps.algorithms.DijkstraAlgorithm;
import cr.ac.una.maps.algorithms.FloydWarshallAlgorithm;
import cr.ac.una.maps.algorithms.PathfindingAlgorithm;
import cr.ac.una.maps.model.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

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

    @FXML
    private ComboBox<String> cmbAlgoritmos;

    @FXML
    private Button btnContinuar;

    @FXML
    private Button btnCerrarAbrir;

    private Map<String, PathfindingAlgorithm> algorithms;

    private double x;
    private double y;
    private boolean selectNode;
    MapEdge selectedEdge;
    Set<MapEdge> callesCerradas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        callesCerradas = new HashSet<>();
        selectNode = true;
        grafo = new MapGraph();
        algorithms = new HashMap<>();
        algorithms.put("Dijkstra", new DijkstraAlgorithm());
        algorithms.put("Floyd-Warshall", new FloydWarshallAlgorithm());
        cmbAlgoritmos.getItems().addAll(algorithms.keySet());
        btnContinuar.setVisible(false);
        cmbAlgoritmos.getSelectionModel().selectFirst();

        // Crear el canvas para dibujar nodos
        canvas = new Canvas();
        stackpaneMap.getChildren().add(canvas); // Añadir el canvas al StackPane

        // Crear un segundo canvas para dibujar rutas/aristas
        canvasRoutes = new Canvas();
        stackpaneMap.getChildren().add(canvasRoutes); // Añadir el canvasRoutes al StackPane

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
                inicializarGrafo(); // Inicializar nodos y aristas
            }
        });

        // Manejar clics en el mapa
        stackpaneMap.setOnMouseClicked(event -> {
            x = event.getX();
            y = event.getY();
            manejarClickEnMapa(x, y);
        });
    }

    @FXML
    void onActionBtnIniciar(ActionEvent event) {
        limpiarCanvas();
        if (puntoA != null && puntoB != null) {
            // Calculamos la ruta utilizando el algoritmo seleccionado
            String selectedAlgorithm = cmbAlgoritmos.getValue();
            PathfindingAlgorithm algorithm = algorithms.get(selectedAlgorithm);

            if (algorithm != null) {
                List<MapNode> ruta = algorithm.findPath(grafo, puntoA, puntoB);
                if (ruta != null && !ruta.isEmpty()) {
                    animarRecorridoConCoche(ruta);
                } else {
                    System.out.println("No se encontró una ruta entre los nodos seleccionados.");
                }
            } else {
                System.out.println("No se seleccionó un algoritmo válido.");
            }
        } else {
            System.out.println("Debe seleccionar los puntos de inicio y destino antes de iniciar.");
        }
        dibujarNodo(puntoB, Color.BLUE);
        calcularRuta();
    }

    @FXML
    void onActionBtnNuevaRuta(ActionEvent event) {
        puntoA = null;
        puntoB = null;
        limpiarCanvas();
    }

    @FXML
    void onAcionContinuar(ActionEvent event) {
        System.out.println("Continuar");
    }

    @FXML
    void onBtnCerrarAbrir(ActionEvent event) {
        cerrarCalleSeleccionada();
    }

    private void cerrarCalleSeleccionada() {
        if (selectedEdge != null) {
            if (selectedEdge.isClosed()) {
                selectedEdge.setClosed(false);
                callesCerradas.remove(selectedEdge);
                dibujarCalle(selectedEdge, Color.PAPAYAWHIP);
            } else {
                callesCerradas.add(selectedEdge);
                selectedEdge.setClosed(true);
                dibujarCalle(selectedEdge, Color.RED);
            }
        }
    }

    private void seleccionarCalle() {
        if (!selectNode) {
            double minDistance = Double.MAX_VALUE;
            if (selectedEdge != null) {
                dibujarCalle(selectedEdge, Color.web("0xffff00ff"));// despintar la calle seleccionada
            }
            selectedEdge = null;
            for (MapEdge edge : grafo.getEdges()) {
                double distance = distanceToEdge(x, y, edge);
                if (distance < minDistance) {
                    minDistance = distance;
                    selectedEdge = edge;
                }
            }
            dibujarCalle(selectedEdge, Color.BLUE);
        }
    }

    private double distanceToEdge(double x, double y, MapEdge edge) {
        double x1 = edge.getFrom().getX();
        double y1 = edge.getFrom().getY();
        double x2 = edge.getTo().getX();
        double y2 = edge.getTo().getY();

        double A = x - x1;
        double B = y - y1;
        double C = x2 - x1;
        double D = y2 - y1;

        double dot = A * C + B * D;
        double len_sq = C * C + D * D;
        double param = dot / len_sq;

        double xx, yy;

        if (param < 0 || (x1 == x2 && y1 == y2)) {
            xx = x1;
            yy = y1;
        } else if (param > 1) {
            xx = x2;
            yy = y2;
        } else {
            xx = x1 + param * C;
            yy = y1 + param * D;
        }

        double dx = x - xx;
        double dy = y - yy;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private void dibujarCalle(MapEdge edge, Color color) {
        GraphicsContext gc = canvasRoutes.getGraphicsContext2D();
        gc.setStroke(color);
        gc.setLineWidth(2);
        gc.strokeLine(edge.getFrom().getX(), edge.getFrom().getY(), edge.getTo().getX(), edge.getTo().getY());
    }

    private void limpiarCanvas() {
        btnContinuar.setVisible(false);
        selectNode = true;
        selectedEdge = null;
        GraphicsContext gcNodos = canvas.getGraphicsContext2D();
        GraphicsContext gcRutas = canvasRoutes.getGraphicsContext2D();
        gcNodos.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gcRutas.clearRect(0, 0, canvasRoutes.getWidth(), canvasRoutes.getHeight());
        pintarCallesCerradas();
    }

    private void manejarClickEnMapa(double x, double y) {
        if (selectNode) {
            seleccionarNodo(x, y);
        } else {
            seleccionarCalle();
        }
    }

    private void seleccionarNodo(double x, double y) {
        MapNode nodoClic = encontrarNodoCercano(x, y);

        if (nodoClic == null) {
            System.out.println("No se encontró un nodo cercano.");
            return;
        }

        if (cmbAlgoritmos.getValue() != null) {
            if (puntoA == null) {
                btnContinuar.setVisible(false);
                puntoA = nodoClic;
                dibujarNodo(puntoA, Color.RED);
                puntoB = null;
            } else if (puntoB == null && nodoClic != puntoA) {
                puntoB = nodoClic;
                dibujarNodo(puntoB, Color.BLUE);
                calcularRuta();
            }
        }
    }

    private void calcularRuta() {
        if (puntoB != null) {
            String selectedAlgorithm = cmbAlgoritmos.getValue();
            PathfindingAlgorithm algorithm = algorithms.get(selectedAlgorithm);

            if (algorithm != null) {
                List<MapNode> ruta = algorithm.findPath(grafo, puntoA, puntoB);
                if (ruta != null && !ruta.isEmpty()) {
                    dibujarRuta(ruta);
                    btnContinuar.setVisible(true);
                    selectNode = false;
                } else {
                    System.out.println("No se encontró una ruta entre los nodos seleccionados.");
                }
            } else {
                System.out.println("No se seleccionó un algoritmo válido.");
            }
        }
    }

    private void pintarCallesCerradas() {
        for (MapEdge edge : callesCerradas) {
            dibujarCalle(edge, Color.RED);
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

    private void animarRecorridoConCoche(List<MapNode> ruta) {
        if (ruta == null || ruta.size() < 2) {
            System.out.println("No hay ruta válida para animar.");
            return;
        }

        GraphicsContext gc = canvasRoutes.getGraphicsContext2D();
        Image car = new Image("cr/ac/una/maps/resources/car.png");
        double imageWidth = 30; // Ajuste del ancho del coche
        double imageHeight = 50; // Ajuste del alto del coche

        // Dibujar la ruta completa una sola vez
        dibujarRuta(ruta);

        // Variables para controlar el estado de la animación
        final int[] currentSegment = {0}; // Segmento actual de la ruta
        final double[] currentX = {ruta.get(0).getX()};
        final double[] currentY = {ruta.get(0).getY()};
        final double[] targetX = {ruta.get(1).getX()};
        final double[] targetY = {ruta.get(1).getY()};

        // Calcular el vector de dirección normalizado y la velocidad del movimiento
        final double velocidad = 1.5; // Ajuste de la velocidad del coche (mayor valor = más rápido)
        final double[] deltaX = {targetX[0] - currentX[0]};
        final double[] deltaY = {targetY[0] - currentY[0]};
        final double[] distancia = {Math.sqrt(deltaX[0] * deltaX[0] + deltaY[0] * deltaY[0])};
        final double[] dirX = {(deltaX[0] / distancia[0]) * velocidad};
        final double[] dirY = {(deltaY[0] / distancia[0]) * velocidad};

        // Crear la animación usando Timeline de JavaFX
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), e -> {
            // Actualizar la posición actual
            currentX[0] += dirX[0];
            currentY[0] += dirY[0];

            // Verificar si hemos alcanzado el destino
            if (Math.abs(currentX[0] - targetX[0]) < velocidad && Math.abs(currentY[0] - targetY[0]) < velocidad) {
                // Ajustar la posición final al nodo objetivo
                currentX[0] = targetX[0];
                currentY[0] = targetY[0];

                // Mover al siguiente segmento si no hemos terminado la ruta
                if (currentSegment[0] < ruta.size() - 2) {
                    currentSegment[0]++;
                    MapNode inicio = ruta.get(currentSegment[0]);
                    MapNode fin = ruta.get(currentSegment[0] + 1);
                    targetX[0] = fin.getX();
                    targetY[0] = fin.getY();

                    // Calcular el nuevo vector de dirección
                    deltaX[0] = targetX[0] - inicio.getX();
                    deltaY[0] = targetY[0] - inicio.getY();
                    distancia[0] = Math.sqrt(deltaX[0] * deltaX[0] + deltaY[0] * deltaY[0]);
                    dirX[0] = (deltaX[0] / distancia[0]) * velocidad;
                    dirY[0] = (deltaY[0] / distancia[0]) * velocidad;
                } else {
                    // Si llegamos al final de la ruta, detener la animación
                    timeline.stop();
                }
            }

            // Limpiar solo la parte necesaria sin borrar la ruta
            gc.clearRect(0, 0, canvasRoutes.getWidth(), canvasRoutes.getHeight());

            // Redibujar la ruta y calles cerradas
            pintarCallesCerradas();
            dibujarRuta(ruta);

            // Dibujar la imagen del coche en la posición actual
            double angle = Math.atan2(dirY[0], dirX[0]); // Ángulo en radianes
            double angleDegrees = Math.toDegrees(angle); // Convertir el ángulo a grados
            double imageOrientationOffset = -90; // Supongamos que la imagen apunta hacia arriba originalmente
            double finalAngleDegrees = angleDegrees + imageOrientationOffset;

            gc.save();
            gc.translate(currentX[0], currentY[0]); // Trasladar al punto actual
            gc.rotate(finalAngleDegrees); // Aplicar la rotación
            gc.drawImage(car, -imageWidth / 2, -imageHeight / 2, imageWidth, imageHeight);
            gc.restore();
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    private void dibujarNodo(MapNode nodo, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(color);
        gc.fillOval(nodo.getX() - 5, nodo.getY() - 5, 10, 10); // Dibuja un círculo
    }

    private void dibujarRuta(List<MapNode> ruta) {
        GraphicsContext gc = canvasRoutes.getGraphicsContext2D(); // Usar el canvas de rutas
        gc.setStroke(Color.GREEN);
        gc.setLineWidth(3);

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
        // grafo.addEdge("NC1", "R", calcularDistancia(507, 423, 484, 505), false);

        grafo.addEdge("NC2", "D", calcularDistancia(462, 247, 439, 241), false);
        // grafo.addEdge("NC2", "I", calcularDistancia(462, 247, 507, 261), false);
        grafo.addEdge("NC2", "NC3", calcularDistancia(462, 247, 486, 176), false);

        // grafo.addEdge("NC3", "Y", calcularDistancia(486, 176, 440, 157), false);
        grafo.addEdge("NC3", "NC2", calcularDistancia(486, 176, 462, 247), false);
        grafo.addEdge("NC3", "T", calcularDistancia(486, 176, 530, 191), false);

        grafo.addEdge("NC4", "A", calcularDistancia(331, 350, 318, 340), false);
        grafo.addEdge("NC4", "L", calcularDistancia(331, 350, 297, 434), false);
        grafo.addEdge("NC4", "F", calcularDistancia(331, 350, 388, 373), false);

        // grafo.addEdge("NC5", "H", calcularDistancia(516, 347, 483, 330), false);
        // grafo.addEdge("NC5", "NC6", calcularDistancia(516, 347, 487, 417), false);
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

            System.out.println("Dibujando arista desde " + edge.getFrom().getId() + " hasta " + edge.getTo().getId()
                    + " en color " + color.toString());
            gc.strokeLine(x1, y1, x2, y2); // Dibuja la línea entre los nodos
        } else {
            System.err.println("Canvas no ha sido inicializado. No se puede dibujar.");
        }
    }

    private double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    @Override
    public void initialize() {
    }
}
