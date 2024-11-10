package cr.ac.una.maps.algorithms;

import cr.ac.una.maps.model.MapGraph;
import cr.ac.una.maps.model.MapNode;

import java.util.ArrayList;
import java.util.List;

public class FloydWarshallAlgorithm implements PathfindingAlgorithm {
    @Override
    public List<MapNode> findPath(MapGraph graph, MapNode start, MapNode end) {
        double[][] shortestPaths = graph.floydWarshall();
        int[][] next = graph.getNextMatrix();
        int startNodeIndex = new ArrayList<>(graph.getNodes()).indexOf(start);
        int endNodeIndex = new ArrayList<>(graph.getNodes()).indexOf(end);
        return graph.getPathFromFloydWarshall(startNodeIndex, endNodeIndex, next);
    }
}