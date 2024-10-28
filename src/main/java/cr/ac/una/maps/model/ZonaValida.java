package cr.ac.una.maps.model;

import java.awt.Polygon;
import java.awt.geom.Point2D;

public class ZonaValida {
    private Polygon poligono;

    public ZonaValida(int[] xPoints, int[] yPoints, int numPoints) {
        this.poligono = new Polygon(xPoints, yPoints, numPoints);
    }

    // Verifica si un punto está dentro de la zona válida (polígono)
    public boolean contiene(double x, double y) {
        return poligono.contains(x, y);
    }

    // Calcula la distancia desde un punto (x, y) al borde del polígono
    public double calcularDistanciaA(double x, double y) {
        double distanciaMinima = Double.MAX_VALUE;

        // Recorremos todos los lados del polígono y calculamos la distancia del punto a cada lado
        for (int i = 0; i < poligono.npoints - 1; i++) {
            double x1 = poligono.xpoints[i];
            double y1 = poligono.ypoints[i];
            double x2 = poligono.xpoints[i + 1];
            double y2 = poligono.ypoints[i + 1];

            // Calculamos la distancia desde el punto hasta el segmento de línea del polígono
            double distancia = distanciaAlSegmento(x, y, x1, y1, x2, y2);
            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
            }
        }

        return distanciaMinima;
    }

    // Encuentra el punto más cercano en los bordes del polígono
    public double[] encontrarPuntoMasCercano(double x, double y) {
        double distanciaMinima = Double.MAX_VALUE;
        double[] puntoMasCercano = new double[2];

        // Recorremos los lados del polígono para encontrar el punto más cercano en el borde
        for (int i = 0; i < poligono.npoints - 1; i++) {
            double x1 = poligono.xpoints[i];
            double y1 = poligono.ypoints[i];
            double x2 = poligono.xpoints[i + 1];
            double y2 = poligono.ypoints[i + 1];

            // Encontramos el punto más cercano en el segmento del polígono
            double[] puntoCercano = puntoMasCercanoEnSegmento(x, y, x1, y1, x2, y2);
            double distancia = Point2D.distance(x, y, puntoCercano[0], puntoCercano[1]);

            if (distancia < distanciaMinima) {
                distanciaMinima = distancia;
                puntoMasCercano = puntoCercano;
            }
        }

        return puntoMasCercano;
    }

    // Calcula la distancia entre un punto (x, y) y un segmento de línea definido por (x1, y1) y (x2, y2)
    private double distanciaAlSegmento(double x, double y, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (dx == 0 && dy == 0) {
            // El segmento es un solo punto
            return Point2D.distance(x, y, x1, y1);
        }

        // Proyección del punto sobre el segmento
        double t = ((x - x1) * dx + (y - y1) * dy) / (dx * dx + dy * dy);

        if (t < 0) {
            return Point2D.distance(x, y, x1, y1); // Más cercano a (x1, y1)
        } else if (t > 1) {
            return Point2D.distance(x, y, x2, y2); // Más cercano a (x2, y2)
        }

        // Proyectado en el medio del segmento
        double proyX = x1 + t * dx;
        double proyY = y1 + t * dy;
        return Point2D.distance(x, y, proyX, proyY);
    }

    // Encuentra el punto más cercano en un segmento de línea
    private double[] puntoMasCercanoEnSegmento(double x, double y, double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (dx == 0 && dy == 0) {
            return new double[]{x1, y1}; // El segmento es un solo punto
        }

        // Proyección del punto sobre el segmento
        double t = ((x - x1) * dx + (y - y1) * dy) / (dx * dx + dy * dy);

        if (t < 0) {
            return new double[]{x1, y1}; // Más cercano a (x1, y1)
        } else if (t > 1) {
            return new double[]{x2, y2}; // Más cercano a (x2, y2)
        }

        // Proyectado en el medio del segmento
        double proyX = x1 + t * dx;
        double proyY = y1 + t * dy;
        return new double[]{proyX, proyY};
    }
}
