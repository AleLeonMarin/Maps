package cr.ac.una.maps.controller;

import cr.ac.una.maps.model.MapNode;
import cr.ac.una.maps.util.AppContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class RutasFinalesController extends Controller implements Initializable {

    @FXML
    private Button btnRutaInicial;

    @FXML
    private Button btnRutaPropuesta;

    @FXML
    private Button btnRutaRealizada;

    @FXML
    private Canvas canvasRoutes;

    @FXML
    private ImageView imvMap;

    @FXML
    private StackPane stackpaneMap;

    private List<MapNode> rutaInicial;
    private List<MapNode> rutaPropuesta;
    private List<MapNode> rutaRealizada;

    @FXML
    private TextFlow textCostoTotal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @Override
    public void initialize() {
        // Limpiar el canvas
        GraphicsContext gc = canvasRoutes.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasRoutes.getWidth(), canvasRoutes.getHeight());

        // Recupera las rutas y los costos desde AppContext
        rutaInicial = (List<MapNode>) AppContext.getInstance().get("rutaInicial");
        rutaPropuesta = (List<MapNode>) AppContext.getInstance().get("rutaPropuesta");
        rutaRealizada = (List<MapNode>) AppContext.getInstance().get("rutaRealizada");

        double costoTotal = (double) AppContext.getInstance().get("costoTotal");
        double costoTotalPeso = (double) AppContext.getInstance().get("costoTotalPeso");
        double costoTotalDetencion = (double) AppContext.getInstance().get("costoTotalDetencion");
        double costoTotalAcumulado = (double) AppContext.getInstance().get("costoTotalAcumulado");

        // Mostrar los costos en el TextFlow
        mostrarCostos(costoTotal, costoTotalPeso, costoTotalDetencion, costoTotalAcumulado);

        stackpaneMap.widthProperty().addListener((obs, oldVal, newVal) -> canvasRoutes.setWidth(newVal.doubleValue()));
        stackpaneMap.heightProperty().addListener((obs, oldVal, newVal) -> canvasRoutes.setHeight(newVal.doubleValue()));
    }

    // Método para dibujar una ruta en el canvas, incluyendo puntos al inicio y final
    private void dibujarRuta(List<MapNode> ruta, Color color) {
        GraphicsContext gc = canvasRoutes.getGraphicsContext2D();
        gc.clearRect(0, 0, canvasRoutes.getWidth(), canvasRoutes.getHeight()); // Limpiar el canvas antes de dibujar
        gc.setStroke(color);
        gc.setLineWidth(3);

        for (int i = 0; i < ruta.size() - 1; i++) {
            MapNode inicio = ruta.get(i);
            MapNode fin = ruta.get(i + 1);
            gc.strokeLine(inicio.getX(), inicio.getY(), fin.getX(), fin.getY());
        }

        // Dibujar punto al inicio de la ruta
        if (!ruta.isEmpty()) {
            MapNode startNode = ruta.get(0);
            gc.setFill(Color.YELLOW); // Color para el punto de inicio
            gc.fillOval(startNode.getX() - 5, startNode.getY() - 5, 10, 10); // Círculo pequeño en el inicio
        }

        // Dibujar punto al final de la ruta
        if (ruta.size() > 1) {
            MapNode endNode = ruta.get(ruta.size() - 1);
            gc.setFill(Color.RED); // Color para el punto de final
            gc.fillOval(endNode.getX() - 5, endNode.getY() - 5, 10, 10); // Círculo pequeño en el final
        }
    }

    @FXML
    void onClickBtnRutaIncial(ActionEvent event) {
        if (rutaInicial != null) {
            dibujarRuta(rutaInicial, Color.BLUE);
        }
    }

    @FXML
    void onClickBtnRutaPropuesta(ActionEvent event) {
        if (rutaPropuesta != null) {
            dibujarRuta(rutaPropuesta, Color.GREEN);
        }
    }

    @FXML
    void onClickBtnRutaReaizada(ActionEvent event) {
        if (rutaRealizada != null) {
            dibujarRuta(rutaRealizada, Color.RED);
        }
    }

    private void mostrarCostos(double costoTotal, double costoTotalPeso, double costoTotalDetencion, double costoTotalAcumulado) {
        textCostoTotal.getChildren().clear();

        Text titulo = new Text("Detalles de Costo de la Ruta:\n");
        titulo.getStyleClass().add("costo-total-titulo");

        Text valorTotal = new Text(String.format("Costo Ruta: %.2f\n", costoTotal));
        valorTotal.getStyleClass().add("costo-total-destacado");

        Text valorPeso = new Text(String.format("Costo por Peso: %.2f\n", costoTotalPeso));
        valorPeso.getStyleClass().add("costo-total-valor");

        Text valorDetencion = new Text(String.format("Costo por Detención: %.2f\n", costoTotalDetencion));
        valorDetencion.getStyleClass().add("costo-total-valor");

        Text valorAcumulado = new Text(String.format("Costo Total Acumulado: %.2f\n", costoTotalAcumulado));
        valorAcumulado.getStyleClass().add("costo-total-destacado");

        textCostoTotal.getChildren().addAll(titulo, valorTotal, valorPeso, valorDetencion, valorAcumulado);
    }
}

