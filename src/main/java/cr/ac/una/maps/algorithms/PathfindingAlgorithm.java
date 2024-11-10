package cr.ac.una.maps.algorithms;

import cr.ac.una.maps.model.MapGraph;
import cr.ac.una.maps.model.MapNode;

import java.util.List;

public interface PathfindingAlgorithm {
    List<MapNode> findPath(MapGraph graph, MapNode start, MapNode end);
}