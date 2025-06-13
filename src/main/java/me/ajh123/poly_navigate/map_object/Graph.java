package me.ajh123.poly_navigate.map_object;

import net.minecraft.util.Identifier;
import org.jgrapht.graph.SimpleGraph;

public class Graph {
    private final SimpleGraph<Node, Way> graph = new SimpleGraph<>(Way.class);
    private final Identifier dimension;

    public Graph(Identifier dimension) {
        this.dimension = dimension;
    }

    public SimpleGraph<Node, Way> getGraph() {
        return graph;
    }

    public Identifier getDimension() {
        return dimension;
    }

    public void addNode(Node node) {
        graph.addVertex(node);
    }

    public Node getNode(int id) {
        for (Node node : graph.vertexSet()) {
            if (node.id() == id) {
                return node;
            }
        }
        return null;
    }

    public void addWay(Way way, Node source, Node target) {
        graph.addEdge(source, target, way);
    }

    public Way getWay(int id) {
        for (Way way : graph.edgeSet()) {
            if (way.id() == id) {
                return way;
            }
        }
        return null;
    }

    public void removeNode(Node node) {
        graph.removeVertex(node);
    }

    public void removeWay(Way way) {
        graph.removeEdge(way);
    }

    public boolean containsNode(Node node) {
        return graph.containsVertex(node);
    }

    public boolean containsWay(Way way) {
        return graph.containsEdge(way);
    }

    public int getNodeCount() {
        return graph.vertexSet().size();
    }

    public int getWayCount() {
        return graph.edgeSet().size();
    }
}
