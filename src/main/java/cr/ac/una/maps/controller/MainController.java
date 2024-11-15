package cr.ac.una.maps.controller;

import com.sun.javafx.geom.Edge;
import cr.ac.una.maps.algorithms.DijkstraAlgorithm;
import cr.ac.una.maps.algorithms.FloydWarshallAlgorithm;
import cr.ac.una.maps.algorithms.PathfindingAlgorithm;
import cr.ac.una.maps.model.*;
import cr.ac.una.maps.util.Mensaje;
import io.github.palexdev.materialfx.controls.MFXSlider;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class MainController extends Controller implements Initializable {

    private MapGraph grafo; // Grafo del mapa
    private List<MapNode> puntosRuta; // Lista de puntos de la ruta
    private Canvas canvas;
    private Canvas canvasRoutes;

    private int currentSegment = 0;


    private static final double COSTO_TRAFICO_LIVIANO = 0.2;
    private static final double COSTO_TRAFICO_MODERADO = 0.5;
    private static final double COSTO_TRAFICO_PESADO = 1.0;


    @FXML
    private Button btnPauseResume;

    @FXML
    private ImageView imvMap;

    @FXML
    private StackPane stackpaneMap;

    @FXML
    private ComboBox<String> cmbAlgoritmos;


    @FXML
    private Button btnCerrarAbrir;
    @FXML
    private Button btnSelectEdge;

    @FXML
    private Button btnAccident;

    @FXML
    private MFXSlider sliderTransito;

    @FXML
    private VBox vboxTransito;

    private List<MapNode> rutaPropuesta;

    private List<MapNode> rutaRealizada;

    private List<MapNode> rutaInicial;

    private Map<String, PathfindingAlgorithm> algorithms;

    private double x;
    private double y;
    private boolean isSelectingNode;
    MapEdge selectedEdge;
    private Set<MapEdge> callesCerradas;
    private Set<MapEdge> callesConTransitoPesado;
    private Set<MapEdge> callesConTransitoMedio;

    private Timeline timeline;
    private boolean isPaused = false;

    @FXML
    private TextFlow textCostoTotal;

    private Map<MapEdge, List<javafx.scene.Node>> accidentMarkers = new HashMap<>();


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        puntosRuta = new ArrayList<>();
        callesCerradas = new HashSet<>();
        callesConTransitoPesado = new HashSet<>();
        callesConTransitoMedio = new HashSet<>();
        isSelectingNode = true;
        grafo = new MapGraph();
        algorithms = new HashMap<>();
        algorithms.put("Dijkstra", new DijkstraAlgorithm());
        algorithms.put("Floyd-Warshall", new FloydWarshallAlgorithm());
        cmbAlgoritmos.getItems().addAll(algorithms.keySet());
        cmbAlgoritmos.getSelectionModel().selectFirst();
        this.vboxTransito.setVisible(false);

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

        stackpaneMap.setOnMouseClicked(event -> {
            x = event.getX();
            y = event.getY();
            guardarCoordenadasDeClick(x, y);
            manejarClickEnMapa(x, y);
        });

        btnPauseResume.setText("Pausar");

        btnPauseResume.setOnAction(event -> {
            togglePauseResume();
        });

        sliderTransito.setMin(1);
        sliderTransito.setMax(3);
        sliderTransito.setValue(1);

        sliderTransito.valueProperty().addListener((obs) -> {
            setPesoEdge();
        });
    }

    @FXML
    void onActionBtnIniciar(ActionEvent event) {
        if (puntosRuta.size() < 2) {
            this.vboxTransito.setVisible(false);
            new Mensaje().showModal(Alert.AlertType.INFORMATION, "Rutas", getStage(), "Debe seleccionar al menos dos puntos para iniciar la ruta.");
            return;
        }

        limpiarCanvas();

        if (timeline != null) {
            timeline.stop();
            timeline = null;
            isPaused = false;
            btnPauseResume.setText("Pausar");
        }

        String selectedAlgorithm = cmbAlgoritmos.getValue();
        PathfindingAlgorithm algorithm = algorithms.get(selectedAlgorithm);

        if (algorithm != null) {
            List<MapNode> rutaCompleta = new ArrayList<>();
            for (int i = 0; i < puntosRuta.size() - 1; i++) {
                MapNode puntoInicial = puntosRuta.get(i);
                MapNode puntoFinal = puntosRuta.get(i + 1);
                List<MapNode> ruta = algorithm.findPath(grafo, puntoInicial, puntoFinal);
                if (ruta != null && !ruta.isEmpty()) {
                    if (i == 0) {
                        rutaCompleta.addAll(ruta);
                    } else {
                        rutaCompleta.addAll(ruta.subList(1, ruta.size()));
                    }
                } else {
                    System.out.println("No se encontró una ruta entre los nodos seleccionados.");
                }
            }

            if (!rutaCompleta.isEmpty()) {
                rutaInicial = List.copyOf(rutaCompleta);
                rutaPropuesta = List.copyOf(rutaInicial);

                // Calcular y mostrar el costo inicial de la ruta
                calcularCostoInicialRuta(rutaCompleta);

                // Iniciar la animación de la ruta
                animarRecorridoConCoche(rutaCompleta);
            }

        }

        for (MapNode punto : puntosRuta) {
            dibujarNodo(punto, Color.RED);
        }
    }


    @FXML
    void onActionBtnSelectEdge(ActionEvent event) {
        this.isSelectingNode = false;
        this.vboxTransito.setVisible(true);
        new Mensaje().showModal(Alert.AlertType.INFORMATION, "Rutas", getStage(), "Ya puedes selccionar rutas, recuerda que puedes ir seleccionando rutas haciendo click sobre ellas");
    }

    @FXML
    void onActionBtnAccident(ActionEvent event) {
        if (selectedEdge != null) {
            selectedEdge.setHasAccident(!selectedEdge.isHasAccident());
            if (selectedEdge.isHasAccident()) {
                selectedEdge.setClosed(true);
                callesCerradas.add(selectedEdge);
                dibujarCalle(selectedEdge, Color.RED);
                new Mensaje().showModal(Alert.AlertType.INFORMATION, "Rutas", getStage(), "Se ha reportado un accidente, la calle está cerrada");
            } else {
                selectedEdge.setClosed(false);
                callesCerradas.remove(selectedEdge);
                accidentMarkers.remove(selectedEdge);
                dibujarCalle(selectedEdge, Color.BLACK);
                new Mensaje().showModal(Alert.AlertType.INFORMATION, "Rutas", getStage(), "Se ha removido el accidente");
            }
            pintarCallesCerradas();
        } else {
            new Mensaje().showModal(Alert.AlertType.INFORMATION, "Rutas", getStage(), "Debes seleccionar una ruta para poder reportar un accidente");
        }
    }


    private static final double COSTO_POR_PESO = 1.5; // Coste por unidad de peso
    private static final double COSTO_POR_SEGUNDO_DE_DETENCION = 0.5; // Coste por segundo detenido


    @FXML
    void onActionBtnNuevaRuta(ActionEvent event) {
        this.vboxTransito.setVisible(false);
        puntosRuta.clear();
        limpiarCanvas();
        pintarCallesCerradas();
        textCostoTotal.getChildren().clear(); // Limpiar el TextFlow para iniciar desde cero
        new Mensaje().showModal(Alert.AlertType.INFORMATION, "Rutas", getStage(), "Estamos listos para una nueva ruta, selecciona los puntos de inicio y fin para nuestro viaje");

        if (timeline != null) {
            timeline.stop();
            timeline = null;
            isPaused = false;
            btnPauseResume.setText("Pausar");
        }
    }


    @FXML
    void onBtnCerrarAbrir(ActionEvent event) {
        if (selectedEdge != null) {
            if (selectedEdge.isClosed()) {
                selectedEdge.setClosed(false);
                callesCerradas.remove(selectedEdge);
                dibujarCalle(selectedEdge, Color.BLACK);
                new Mensaje().showModal(Alert.AlertType.INFORMATION, "Rutas", getStage(), "Se ha abierto el carril seleccionado");
            } else {
                selectedEdge.setClosed(true);
                callesCerradas.add(selectedEdge);
                dibujarCalle(selectedEdge, Color.RED);
                new Mensaje().showModal(Alert.AlertType.INFORMATION, "Rutas", getStage(), "Se ha cerrado el carril seleccionado");
            }
            pintarCallesCerradas();
        } else {
            new Mensaje().showModal(Alert.AlertType.INFORMATION, "Rutas", getStage(), "Debes seleccionar una ruta para poder cerrar o abrir una calle");
        }
    }

    private void seleccionarCalle(double x, double y) {
        if (!isSelectingNode) {
            if (selectedEdge != null) {
                dibujarCalle(selectedEdge, Color.BLACK);
                pintarCallesCerradas();
            }
            List<MapEdge> nearbyEdges = new ArrayList<>();
            for (MapEdge edge : grafo.getEdges()) {
                if (distanceToEdge(x, y, edge) < 5.0) {
                    nearbyEdges.add(edge);
                }
            }
            if (!nearbyEdges.isEmpty()) {
                if (selectedEdge != null && nearbyEdges.contains(selectedEdge)) {
                    int currentIndex = nearbyEdges.indexOf(selectedEdge);
                    selectedEdge = nearbyEdges.get((currentIndex + 1) % nearbyEdges.size());
                } else {
                    selectedEdge = nearbyEdges.get(0);
                }
                dibujarCalle(selectedEdge, Color.AQUAMARINE);
                animarFlecha(selectedEdge);
            }
        }
    }

    private void animarFlecha(MapEdge edge) {
        String path = edge.isClosed() ? "cr/ac/una/maps/resources/redArrow.png" : "cr/ac/una/maps/resources/greenArrow.png";
        Image arrowImage = new Image(path);

        double x1 = edge.getFrom().getX();
        double y1 = edge.getFrom().getY();
        double x2 = edge.getTo().getX();
        double y2 = edge.getTo().getY();

        double angle = Math.atan2(y2 - y1, x2 - x1);
        double angleDegrees = Math.toDegrees(angle);

        double midX = (x1 + x2) / 2;
        double midY = (y1 + y2) / 2;

        double offset = 15;
        double offsetX = offset * Math.cos(angle + Math.PI / 2);
        double offsetY = offset * Math.sin(angle + Math.PI / 2);

        GraphicsContext gc = canvasRoutes.getGraphicsContext2D();
        gc.save();
        gc.translate(midX + offsetX, midY + offsetY);
        gc.rotate(angleDegrees);
        gc.drawImage(arrowImage, -10, -10, 20, 20);
        gc.restore();

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), canvasRoutes);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.setCycleCount(1);
        fadeIn.setAutoReverse(false);
        fadeIn.play();

        // Remove the arrow after the animation is complete
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1.0), e -> {
            gc.clearRect(midX + offsetX - 10, midY + offsetY - 10, 20, 20); // Clear the arrow from the canvas
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }

    private void setPesoEdge() {
        if (selectedEdge != null && !isSelectingNode) {
            selectedEdge.setWeight(calcularDistancia(selectedEdge.getFrom().getX(), selectedEdge.getFrom().getY(), selectedEdge.getTo().getX(), selectedEdge.getTo().getY()) * sliderTransito.getValue());
            if (sliderTransito.getValue() >= 3) {
                callesConTransitoPesado.add(selectedEdge);
                callesConTransitoMedio.remove(selectedEdge);
            } else if (sliderTransito.getValue() == 2) {
                callesConTransitoMedio.add(selectedEdge);
                callesConTransitoPesado.remove(selectedEdge);
            } else {
                dibujarCalle(selectedEdge, Color.BLACK);
                callesConTransitoPesado.remove(selectedEdge);
                callesConTransitoMedio.remove(selectedEdge);
            }
        }
        pintarCallesCerradas();
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
        isSelectingNode = true;
        selectedEdge = null;
        GraphicsContext gcNodos = canvas.getGraphicsContext2D();
        GraphicsContext gcRutas = canvasRoutes.getGraphicsContext2D();
        gcNodos.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gcRutas.clearRect(0, 0, canvasRoutes.getWidth(), canvasRoutes.getHeight());
    }

    private void manejarClickEnMapa(double x, double y) {
        if (isSelectingNode) {
            seleccionarNodo(x, y);
        } else {
            seleccionarCalle(x, y);
        }
    }

    private void seleccionarNodo(double x, double y) {
        MapNode nodoClic = encontrarNodoCercano(x, y);

        if (nodoClic == null) {
            System.out.println("No se encontró un nodo cercano.");
            return;
        }

        if (puntosRuta.contains(nodoClic)) {
            // Si el nodo ya está en la lista, se elimina
            puntosRuta.remove(nodoClic);

            // Limpiar el canvas de nodos y rutas
            limpiarCanvas();

            // Redibujar los nodos restantes
            for (MapNode punto : puntosRuta) {
                dibujarNodo(punto, Color.RED);
            }

            // Recalcular y redibujar las rutas entre los nodos restantes
            if (puntosRuta.size() > 1) {
                recalcularYRedibujarRutas();
            }
        } else {
            // Si el nodo no está en la lista, se agrega
            puntosRuta.add(nodoClic);
            dibujarNodo(nodoClic, Color.RED);

            // Recalcular y redibujar las rutas si hay más de un nodo
            if (puntosRuta.size() > 1) {
                recalcularYRedibujarRutas();
            }
        }
    }

    private void recalcularYRedibujarRutas() {
        String selectedAlgorithm = cmbAlgoritmos.getValue();
        PathfindingAlgorithm algorithm = algorithms.get(selectedAlgorithm);

        // Limpiar las rutas en el canvas de rutas
        GraphicsContext gcRutas = canvasRoutes.getGraphicsContext2D();
        gcRutas.clearRect(0, 0, canvasRoutes.getWidth(), canvasRoutes.getHeight());

        // Crear una lista para acumular la ruta completa
        List<MapNode> rutaCompleta = new ArrayList<>();

        // Recalcular y redibujar la ruta para cada par consecutivo de nodos en puntosRuta
        for (int i = 0; i < puntosRuta.size() - 1; i++) {
            MapNode puntoInicial = puntosRuta.get(i);
            MapNode puntoFinal = puntosRuta.get(i + 1);
            List<MapNode> ruta = algorithm.findPath(grafo, puntoInicial, puntoFinal);

            if (ruta != null && !ruta.isEmpty()) {
                if (i == 0) {
                    rutaCompleta.addAll(ruta);
                } else {
                    rutaCompleta.addAll(ruta.subList(1, ruta.size())); // Evitar duplicar el nodo de inicio
                }
            }
        }

        // Dibujar la ruta recalculada
        dibujarRuta(rutaCompleta, Color.GREEN);

        // Calcular y mostrar el costo inicial de la ruta
        calcularCostoInicialRuta(rutaCompleta);
    }


    private void calcularRuta() {
        if (puntosRuta.size() >= 2) {
            String selectedAlgorithm = cmbAlgoritmos.getValue();
            PathfindingAlgorithm algorithm = algorithms.get(selectedAlgorithm);

            if (algorithm != null) {
                int lastIndex = puntosRuta.size() - 1;
                MapNode puntoAnterior = puntosRuta.get(lastIndex - 1);
                MapNode nuevoPunto = puntosRuta.get(lastIndex);
                List<MapNode> ruta = algorithm.findPath(grafo, puntoAnterior, nuevoPunto);

                if (ruta != null && !ruta.isEmpty()) {
                    dibujarRuta(ruta, Color.GREEN);
                    calcularCostoRuta(ruta);
                } else {
                    System.out.println("No se encontró una ruta entre los nodos seleccionados.");
                }
            }
        }
    }

    private void pintarCallesCerradas() {
        pintarCallesConTransitoMedio();
        pintarCallesConTransitoPesado();
        for (MapEdge edge : callesCerradas) {
            if (edge.isClosed()) {
                dibujarCalle(edge, Color.RED);
                if (edge.isHasAccident()) {
                    mostrarMensajeAccidente(edge);
                }
            } else {
                callesCerradas.remove(edge);
            }
        }
    }

    private void pintarCallesConTransitoPesado() {
        for (MapEdge edge : callesConTransitoPesado) {
            if (edge.getWeight() > calcularDistancia(edge.getFrom().getX(), edge.getFrom().getY(), edge.getTo().getX(), edge.getTo().getY()) * 2.5) {
                dibujarCalle(edge, Color.ORANGE);
            } else {
                callesConTransitoPesado.remove(edge);
            }
        }
    }

    private void pintarCallesConTransitoMedio() {
        for (MapEdge edge : callesConTransitoMedio) {
            if ((edge.getWeight() >= calcularDistancia(edge.getFrom().getX(), edge.getFrom().getY(), edge.getTo().getX(), edge.getTo().getY()) * 1.5) && (edge.getWeight() < calcularDistancia(edge.getFrom().getX(), edge.getFrom().getY(), edge.getTo().getX(), edge.getTo().getY()) * 2.5)) {
                dibujarCalle(edge, Color.YELLOW);
            } else {
                callesConTransitoMedio.remove(edge);
            }
        }
    }

    private void mostrarMensajeAccidente(MapEdge edge) {
        GraphicsContext gc = canvasRoutes.getGraphicsContext2D();
        String path = "cr/ac/una/maps/resources/accident.png";
        Image accidentImage = new Image(path);

        double x1 = edge.getFrom().getX();
        double y1 = edge.getFrom().getY();
        double x2 = edge.getTo().getX();
        double y2 = edge.getTo().getY();

        double midX = (x1 + x2) / 2;
        double midY = (y1 + y2) / 2;

        double imageWidth = 35;
        double imageHeight = 35;

        gc.drawImage(accidentImage, midX - imageWidth / 2, midY - imageHeight / 2, imageWidth, imageHeight);

        accidentMarkers.put(edge, Collections.singletonList(new ImageView(accidentImage)));
    }

    private MapNode encontrarNodoCercano(double x, double y) {
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

    private static final double COSTO_POR_LONGITUD = 1.5; // Coste por unidad de longitud (peso)

    private void animarRecorridoConCoche(List<MapNode> ruta) {
        if (ruta == null || ruta.size() < 2) {
            System.out.println("No hay ruta válida para animar.");
            return;
        }

        rutaRealizada = new ArrayList<>();
        GraphicsContext gc = canvasRoutes.getGraphicsContext2D();
        Image car = new Image("cr/ac/una/maps/resources/car.png");
        double imageWidth = 30;
        double imageHeight = 50;

        dibujarRuta(ruta, Color.GREEN);

        final int[] currentSegment = {0};
        final double[] currentX = {ruta.get(0).getX()};
        final double[] currentY = {ruta.get(0).getY()};
        final double[] targetX = {ruta.get(1).getX()};
        final double[] targetY = {ruta.get(1).getY()};

        final double baseVelocidad = 1.0;
        final double[] velocidad = {baseVelocidad};
        final double[] costoTotalPeso = {0.0}; // Costo total por longitud
        final double[] tiempoDetenidoTotal = {0.0}; // Tiempo total detenido
        final double[] deltaX = {targetX[0] - currentX[0]};
        final double[] deltaY = {targetY[0] - currentY[0]};
        final double[] distancia = {Math.sqrt(deltaX[0] * deltaX[0] + deltaY[0] * deltaY[0])};
        final double[] dirX = {(deltaX[0] / distancia[0]) * velocidad[0]};
        final double[] dirY = {(deltaY[0] / distancia[0]) * velocidad[0]};

        rutaRealizada.add(ruta.get(0));

        timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), e -> {
            if (!isPaused) {
                // Ajustar la velocidad según el tráfico
                MapNode inicio = ruta.get(currentSegment[0]);
                MapNode fin = ruta.get(currentSegment[0] + 1);
                MapEdge edge = grafo.getEdge(inicio, fin);
                if (edge != null) {
                    if (callesConTransitoPesado.contains(edge)) {
                        velocidad[0] = baseVelocidad * 0.3; // Reducir la velocidad a 30%
                    } else if (callesConTransitoMedio.contains(edge)) {
                        velocidad[0] = baseVelocidad * 0.5; // Reducir la velocidad a 50%
                    } else {
                        velocidad[0] = baseVelocidad; // Velocidad normal
                    }
                    dirX[0] = (deltaX[0] / distancia[0]) * velocidad[0];
                    dirY[0] = (deltaY[0] / distancia[0]) * velocidad[0];


                }

                // Movimiento en la ruta
                currentX[0] += dirX[0];
                currentY[0] += dirY[0];

                if (Math.abs(currentX[0] - targetX[0]) < velocidad[0] && Math.abs(currentY[0] - targetY[0]) < velocidad[0]) {
                    currentX[0] = targetX[0];
                    currentY[0] = targetY[0];

                    rutaRealizada.add(ruta.get(currentSegment[0] + 1));

                    if (currentSegment[0] < ruta.size() - 2) {
                        currentSegment[0]++;
                        inicio = ruta.get(currentSegment[0]);
                        fin = ruta.get(currentSegment[0] + 1);
                        targetX[0] = fin.getX();
                        targetY[0] = fin.getY();

                        deltaX[0] = targetX[0] - inicio.getX();
                        deltaY[0] = targetY[0] - inicio.getY();
                        distancia[0] = Math.sqrt(deltaX[0] * deltaX[0] + deltaY[0] * deltaY[0]);
                        dirX[0] = (deltaX[0] / distancia[0]) * velocidad[0];
                        dirY[0] = (deltaY[0] / distancia[0]) * velocidad[0];

                        if (edge != null) {
                            costoTotalPeso[0] += edge.getWeight() * COSTO_POR_LONGITUD;
                        }
                    } else {
                        timeline.stop();
                        // Cálculo final del costo total
                        double costoPorLongitud = edge.getWeight() * COSTO_POR_LONGITUD;
                        costoTotalPeso[0] += costoPorLongitud;
                        double costoDetencionAdicional = edge.getWeight() * (callesConTransitoPesado.contains(edge) ? COSTO_TRAFICO_PESADO :
                                callesConTransitoMedio.contains(edge) ? COSTO_TRAFICO_MODERADO : COSTO_TRAFICO_LIVIANO);
                        tiempoDetenidoTotal[0] += costoDetencionAdicional;

                        mostrarCostoTotal(costoTotalPeso[0] + tiempoDetenidoTotal[0], costoTotalPeso[0], tiempoDetenidoTotal[0]);

                        new Mensaje().show(Alert.AlertType.INFORMATION, "Rutas", "Llegaste a tu destino.");
                        // Al finalizar, limpiar y dibujar la ruta propuesta y la ruta realizada en los colores adecuados
                        gc.clearRect(0, 0, canvasRoutes.getWidth(), canvasRoutes.getHeight());
                        pintarCallesCerradas();
                        dibujarRuta(ruta, Color.LIGHTBLUE);
                        dibujarRutaConCombinacion(rutaRealizada, rutaPropuesta);
                        return;
                    }
                }

                gc.clearRect(0, 0, canvasRoutes.getWidth(), canvasRoutes.getHeight());
                pintarCallesCerradas();
                dibujarRuta(ruta, Color.LIGHTBLUE);

                double angle = Math.atan2(dirY[0], dirX[0]);
                double angleDegrees = Math.toDegrees(angle);
                double imageOrientationOffset = -90;
                double finalAngleDegrees = angleDegrees + imageOrientationOffset;

                gc.save();
                gc.translate(currentX[0], currentY[0]);
                gc.rotate(finalAngleDegrees);
                gc.drawImage(car, -imageWidth / 2, -imageHeight / 2, imageWidth, imageHeight);
                gc.restore();
            } else {
                // Si está pausado, acumula el tiempo de detención
                tiempoDetenidoTotal[0] += 0.02; // Tiempo detenido en segundos
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    private void togglePauseResume() {
        if (!isPaused && timeline == null) {
            // Si no está pausado, muestra el mensaje y sale del método
            new Mensaje().showModal(Alert.AlertType.INFORMATION, "Rutas", getStage(), "Primero debes iniciar la ruta para poder pausarla.");
            return;
        }

        if (isPaused) {
            // Al reanudar, recalcular la ruta completa con los nuevos puntos añadidos.
            if (puntosRuta.size() >= 2) {
                // Detener el `timeline` actual si está en ejecución.
                if (timeline != null) {
                    timeline.stop();
                }

                // Obtener la ruta completa desde el punto actual en adelante
                List<MapNode> rutaCompleta = new ArrayList<>();
                for (int i = currentSegment; i < puntosRuta.size() - 1; i++) {
                    MapNode puntoInicial = puntosRuta.get(i);
                    MapNode puntoFinal = puntosRuta.get(i + 1);
                    List<MapNode> ruta = algorithms.get(cmbAlgoritmos.getValue()).findPath(grafo, puntoInicial, puntoFinal);
                    if (ruta != null && !ruta.isEmpty()) {
                        rutaCompleta.addAll(ruta.subList(i == currentSegment ? 0 : 1, ruta.size()));
                    }
                }

                animarRecorridoConCoche(rutaCompleta);
            }

            btnPauseResume.setText("Pausar");
        } else {
            timeline.pause();
            btnPauseResume.setText("Reanudar");
        }
        isPaused = !isPaused;
    }


    private void dibujarNodo(MapNode nodo, Color color) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(color);
        gc.fillOval(nodo.getX() - 5, nodo.getY() - 5, 10, 10); // Dibuja un círculo
    }

    private void dibujarRuta(List<MapNode> ruta, Color color) {
        GraphicsContext gc = canvasRoutes.getGraphicsContext2D();
        gc.setStroke(color); // Usar el color pasado como parámetro
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
        pintarCallesCerradas();
    }

    private void dibujarRutaConCombinacion(List<MapNode> rutaRealizada, List<MapNode> rutaPropuesta) {
        GraphicsContext gc = canvasRoutes.getGraphicsContext2D();
        gc.setLineWidth(3);

        if (rutaRealizada.isEmpty() && rutaPropuesta.isEmpty()) {
            System.out.println("No se encontró una ruta entre los puntos.");
            return;
        }

        // Crear un conjunto de segmentos de la ruta realizada
        Set<String> segmentosRealizada = new HashSet<>();
        for (int i = 0; i < rutaRealizada.size() - 1; i++) {
            MapNode inicio = rutaRealizada.get(i);
            MapNode fin = rutaRealizada.get(i + 1);
            segmentosRealizada.add(crearSegmentoId(inicio, fin));
        }

        // Crear un conjunto de segmentos en común entre rutaRealizada y rutaPropuesta
        Set<String> segmentosComunes = new HashSet<>();
        for (int i = 0; i < rutaPropuesta.size() - 1; i++) {
            MapNode inicio = rutaPropuesta.get(i);
            MapNode fin = rutaPropuesta.get(i + 1);
            String segmentoId = crearSegmentoId(inicio, fin);

            // Si el segmento también está en rutaRealizada, es común
            if (segmentosRealizada.contains(segmentoId)) {
                segmentosComunes.add(segmentoId);
            }
        }

        // Verificar si hay segmentos comunes o no
        boolean haySegmentosComunes = !segmentosComunes.isEmpty();

        // Dibujar la ruta realizada
        for (int i = 0; i < rutaRealizada.size() - 1; i++) {
            MapNode inicio = rutaRealizada.get(i);
            MapNode fin = rutaRealizada.get(i + 1);
            String segmentoId = crearSegmentoId(inicio, fin);

            // Si el segmento es común, usar color combinado
            if (haySegmentosComunes && segmentosComunes.contains(segmentoId)) {
                gc.setStroke(Color.AQUAMARINE); // Color combinado para segmentos comunes
            } else {
                gc.setStroke(Color.LAVENDER); // Color normal para la ruta realizada
            }
            gc.strokeLine(inicio.getX(), inicio.getY(), fin.getX(), fin.getY());
        }

        // Dibujar la ruta propuesta
        for (int i = 0; i < rutaPropuesta.size() - 1; i++) {
            MapNode inicio = rutaPropuesta.get(i);
            MapNode fin = rutaPropuesta.get(i + 1);
            String segmentoId = crearSegmentoId(inicio, fin);

            // Si el segmento es común y ya ha sido dibujado, omitir
            if (haySegmentosComunes && segmentosComunes.contains(segmentoId)) {
                continue; // Saltar el dibujo para evitar superposición
            }

            gc.setStroke(Color.BLUEVIOLET); // Color normal para la ruta propuesta
            gc.strokeLine(inicio.getX(), inicio.getY(), fin.getX(), fin.getY());
        }

        pintarCallesCerradas();
    }

    private String crearSegmentoId(MapNode inicio, MapNode fin) {
        return inicio.getId() + "-" + fin.getId();
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


        grafo.addNode("0", 37, 495);
        grafo.addNode("1", 127, 399);
        grafo.addNode("2", 179, 346);
        grafo.addNode("3", 219, 298);
        grafo.addNode("4", 269, 244);
        grafo.addNode("5", 295, 212);
        grafo.addNode("6", 325, 174);
        grafo.addNode("7", 330, 148);
        grafo.addNode("8", 352, 36);


        grafo.addNode("9", 390, 40);
        grafo.addNode("10", 406, 49);
        grafo.addNode("11", 482, 16);
        grafo.addNode("12", 487, 34);
        grafo.addNode("13", 481, 92);
        grafo.addNode("14", 555, 122);
        grafo.addNode("15", 582, 47);
        grafo.addNode("16", 593, 42);
        grafo.addNode("17", 648, 133);


        grafo.addNode("18", 690, 201);
        grafo.addNode("19", 787, 373);
        grafo.addNode("20", 755, 446);
        grafo.addNode("21", 689, 418);
        grafo.addNode("22", 660, 486);
        grafo.addNode("23", 630, 555);
        grafo.addNode("24", 565, 530);

        grafo.addNode("25", 538, 599);
        grafo.addNode("26", 463, 576);
        grafo.addNode("27", 444, 637);
        grafo.addNode("28", 423, 705);

        grafo.addNode("29", 208, 475);
        grafo.addNode("30", 273, 490);
        grafo.addNode("31", 388, 594);
        grafo.addNode("32", 133, 499);


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
        edges.add(grafo.addEdge("O", "P", calcularDistancia(649, 319, 578, 293), false));
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


        edges.add(grafo.addEdge("0", "28", calcularDistancia(37, 495, 423, 705), false));
        edges.add(grafo.addEdge("28", "0", calcularDistancia(423, 705, 37, 495), false));
        edges.add(grafo.addEdge("0", "1", calcularDistancia(37, 495, 127, 401), false));
        edges.add(grafo.addEdge("1", "0", calcularDistancia(127, 401, 37, 495), false));
        edges.add(grafo.addEdge("1", "2", calcularDistancia(127, 401, 180, 348), false));
        edges.add(grafo.addEdge("2", "1", calcularDistancia(180, 348, 127, 401), false));
        edges.add(grafo.addEdge("2", "3", calcularDistancia(180, 348, 221, 301), false));
        edges.add(grafo.addEdge("2", "V", calcularDistancia(180, 348, 223, 384), false));
        edges.add(grafo.addEdge("V", "2", calcularDistancia(223, 384, 180, 348), false));
        edges.add(grafo.addEdge("3", "2", calcularDistancia(221, 301, 180, 348), false));
        edges.add(grafo.addEdge("3", "X", calcularDistancia(221, 301, 267, 318), false));
        edges.add(grafo.addEdge("X", "3", calcularDistancia(267, 318, 221, 301), false));
        edges.add(grafo.addEdge("3", "4", calcularDistancia(221, 301, 269, 247), false));
        edges.add(grafo.addEdge("4", "3", calcularDistancia(269, 247, 221, 301), false));
        edges.add(grafo.addEdge("4", "B", calcularDistancia(269, 247, 349, 274), false));
        edges.add(grafo.addEdge("4", "5", calcularDistancia(269, 247, 299, 214), false));
        edges.add(grafo.addEdge("5", "4", calcularDistancia(299, 214, 269, 247), false));
        edges.add(grafo.addEdge("C", "5", calcularDistancia(369, 230, 299, 214), false));
        edges.add(grafo.addEdge("5", "6", calcularDistancia(299, 214, 325, 174), false));
        edges.add(grafo.addEdge("6", "5", calcularDistancia(325, 174, 299, 214), false));
        edges.add(grafo.addEdge("6", "7", calcularDistancia(325, 174, 333, 148), false));
        edges.add(grafo.addEdge("7", "6", calcularDistancia(333, 148, 325, 174), false));
        edges.add(grafo.addEdge("7", "8", calcularDistancia(333, 148, 352, 36), false));
        edges.add(grafo.addEdge("8", "7", calcularDistancia(352, 36, 333, 148), false));
        edges.add(grafo.addEdge("8", "9", calcularDistancia(352, 36, 390, 40), false));
        edges.add(grafo.addEdge("9", "10", calcularDistancia(390, 40, 406, 49), false));
        edges.add(grafo.addEdge("10", "9", calcularDistancia(406, 49, 390, 40), false));
        edges.add(grafo.addEdge("Y", "10", calcularDistancia(440, 157, 406, 49), false));
        edges.add(grafo.addEdge("10", "11", calcularDistancia(406, 49, 482, 16), false));
        edges.add(grafo.addEdge("11", "10", calcularDistancia(482, 16, 406, 49), false));
        edges.add(grafo.addEdge("11", "12", calcularDistancia(482, 16, 487, 34), false));
        edges.add(grafo.addEdge("12", "11", calcularDistancia(487, 34, 482, 16), false));
        edges.add(grafo.addEdge("12", "13", calcularDistancia(487, 34, 481, 92), false));
        edges.add(grafo.addEdge("13", "12", calcularDistancia(481, 92, 487, 34), false));
        edges.add(grafo.addEdge("13", "14", calcularDistancia(481, 92, 555, 122), false));
        edges.add(grafo.addEdge("14", "13", calcularDistancia(555, 122, 481, 92), false));
        edges.add(grafo.addEdge("14", "T", calcularDistancia(555, 122, 530, 191), false));
        edges.add(grafo.addEdge("14", "Z", calcularDistancia(555, 122, 633, 150), false));
        edges.add(grafo.addEdge("Z", "14", calcularDistancia(633, 150, 555, 122), false));
        edges.add(grafo.addEdge("Z", "17", calcularDistancia(633, 150, 648, 133), false));
        edges.add(grafo.addEdge("15", "14", calcularDistancia(582, 47, 555, 122), false));
        edges.add(grafo.addEdge("16", "15", calcularDistancia(593, 42, 582, 47), false));
        edges.add(grafo.addEdge("16", "17", calcularDistancia(593, 42, 648, 133), false));
        edges.add(grafo.addEdge("17", "16", calcularDistancia(648, 133, 593, 42), false));
        edges.add(grafo.addEdge("17", "18", calcularDistancia(648, 133, 690, 201), false));
        edges.add(grafo.addEdge("18", "17", calcularDistancia(690, 201, 648, 133), false));
        edges.add(grafo.addEdge("18", "AA", calcularDistancia(690, 201, 675, 248), false));
        edges.add(grafo.addEdge("18", "19", calcularDistancia(690, 201, 787, 373), false));
        edges.add(grafo.addEdge("19", "18", calcularDistancia(787, 373, 690, 201), false));
        edges.add(grafo.addEdge("19", "BB", calcularDistancia(787, 373, 716, 347), false));
        edges.add(grafo.addEdge("19", "20", calcularDistancia(787, 373, 755, 446), false));
        edges.add(grafo.addEdge("20", "19", calcularDistancia(755, 446, 787, 373), false));//YA VEREMOS
        edges.add(grafo.addEdge("21", "20", calcularDistancia(689, 418, 755, 446), false));
        edges.add(grafo.addEdge("N", "21", calcularDistancia(623, 390, 689, 418), false));
        edges.add(grafo.addEdge("22", "21", calcularDistancia(660, 486, 689, 418), false));
        edges.add(grafo.addEdge("22", "M", calcularDistancia(660, 486, 596, 458), false));
        edges.add(grafo.addEdge("M", "22", calcularDistancia(596, 458, 660, 486), false));
        edges.add(grafo.addEdge("M", "24", calcularDistancia(596, 458, 565, 530), false));
        edges.add(grafo.addEdge("23", "22", calcularDistancia(630, 555, 660, 486), false));
        edges.add(grafo.addEdge("23", "24", calcularDistancia(630, 555, 565, 530), false));
        edges.add(grafo.addEdge("24", "23", calcularDistancia(565, 530, 630, 555), false));
        edges.add(grafo.addEdge("24", "25", calcularDistancia(565, 530, 538, 599), false));
        edges.add(grafo.addEdge("25", "26", calcularDistancia(538, 599, 463, 576), false));
        edges.add(grafo.addEdge("26", "25", calcularDistancia(463, 576, 538, 599), false));
        edges.add(grafo.addEdge("26", "27", calcularDistancia(463, 576, 444, 637), false));
        edges.add(grafo.addEdge("26", "R", calcularDistancia(463, 576, 484, 505), false));
        edges.add(grafo.addEdge("27", "26", calcularDistancia(444, 637, 463, 576), false));
        edges.add(grafo.addEdge("27", "U", calcularDistancia(444, 637, 297, 576), false));
        edges.add(grafo.addEdge("U", "27", calcularDistancia(297, 576, 444, 637), false));
        edges.add(grafo.addEdge("27", "28", calcularDistancia(444, 637, 423, 705), false));
        edges.add(grafo.addEdge("28", "27", calcularDistancia(423, 705, 444, 637), false));
        edges.add(grafo.addEdge("28", "0", calcularDistancia(423, 705, 37, 495), false));
        edges.add(grafo.addEdge("0", "28", calcularDistancia(37, 495, 423, 705), false));


        edges.add(grafo.addEdge("29", "W", calcularDistancia(208, 475, 249, 406), false));
        edges.add(grafo.addEdge("W", "29", calcularDistancia(249, 406, 208, 475), false));

        edges.add(grafo.addEdge("L", "30", calcularDistancia(297, 434, 273, 490), false));
        edges.add(grafo.addEdge("30", "L", calcularDistancia(273, 490, 297, 434), false));

        edges.add(grafo.addEdge("J", "31", calcularDistancia(425, 484, 388, 594), false));
        edges.add(grafo.addEdge("31", "J", calcularDistancia(388, 594, 425, 484), false));

        edges.add(grafo.addEdge("U", "32", calcularDistancia(297, 576, 133, 499), false));
        edges.add(grafo.addEdge("32", "U", calcularDistancia(133, 499, 297, 576), false));


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

    private void guardarCoordenadasDeClick(double x, double y) {
        System.out.println("Coordenadas del clic: (" + x + ", " + y + ")");
    }

    private void calcularCostoRuta(List<MapNode> ruta) {
        double costoTotalPeso = 0.0;
        double costoTotalDetencion = 0.0;

        for (int i = 0; i < ruta.size() - 1; i++) {
            MapNode inicio = ruta.get(i);
            MapNode fin = ruta.get(i + 1);
            MapEdge edge = grafo.getEdge(inicio, fin);

            if (edge != null) {
                // Costo por longitud o peso de la arista
                double costoPorLongitud = edge.getWeight() * COSTO_POR_LONGITUD;
                costoTotalPeso += costoPorLongitud;

                // Costo adicional basado en el tráfico
                double costoDetencionAdicional = 0.0;
                if (callesConTransitoPesado.contains(edge)) {
                    costoDetencionAdicional = edge.getWeight() * COSTO_TRAFICO_PESADO;
                } else if (callesConTransitoMedio.contains(edge)) {
                    costoDetencionAdicional = edge.getWeight() * COSTO_TRAFICO_MODERADO;
                } else {
                    costoDetencionAdicional = edge.getWeight() * COSTO_TRAFICO_LIVIANO;
                }
                costoTotalDetencion += costoDetencionAdicional;
            }
        }

        double costoTotal = costoTotalPeso + costoTotalDetencion;
        mostrarCostoTotal(costoTotal, costoTotalPeso, costoTotalDetencion);
    }


    private void mostrarCostoTotal(double costoTotal, double costoTotalPeso, double costoTotalDetencion) {
        textCostoTotal.getChildren().clear();
        textCostoTotal.getStyleClass().add("textflow-uber");

        Text titulo = new Text("Detalles de Costo de la Ruta:\n");
        titulo.getStyleClass().add("costo-total-titulo");

        Text valorTotal = new Text(String.format("Costo Total: %.2f\n", costoTotal));
        valorTotal.getStyleClass().add("costo-total-destacado");

        Text valorPeso = new Text(String.format("Costo por Peso: %.2f\n", costoTotalPeso));
        valorPeso.getStyleClass().add("costo-total-valor");

        Text valorDetencion = new Text(String.format("Costo por Detención: %.2f\n", costoTotalDetencion));
        valorDetencion.getStyleClass().add("costo-total-valor");

        textCostoTotal.getChildren().addAll(titulo, valorTotal, valorPeso, valorDetencion);
    }


    private void calcularCostoInicialRuta(List<MapNode> ruta) {
        double costoInicial = 0.0;

        // Recorrer la lista de nodos en la ruta para calcular el costo en base al peso de las aristas
        for (int i = 0; i < ruta.size() - 1; i++) {
            MapNode inicio = ruta.get(i);
            MapNode fin = ruta.get(i + 1);
            MapEdge edge = grafo.getEdge(inicio, fin);

            if (edge != null) {
                // Solo sumamos el peso de la arista, sin considerar tráfico o detenciones
                costoInicial += edge.getWeight() * COSTO_POR_LONGITUD;
            }
        }

        // Mostrar el costo inicial en la interfaz
        mostrarCostoInicial(costoInicial);
    }

    private void mostrarCostoInicial(double costoInicial) {
        textCostoTotal.getChildren().clear();
        textCostoTotal.getStyleClass().add("textflow-uber");

        Text titulo = new Text("Detalles de Costo Inicial de la Ruta:\n");
        titulo.getStyleClass().add("costo-total-titulo");

        Text valorTotal = new Text(String.format("Costo Inicial (Peso de aristas): %.2f\n", costoInicial));
        valorTotal.getStyleClass().add("costo-total-destacado");

        textCostoTotal.getChildren().addAll(titulo, valorTotal);
    }


}
