package peersim.jgrapht;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

import java.io.File;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.AbstractMap;

public class GraphTopology implements EDProtocol {

    static class AsVertex {

        private String id;

        private ArrayList<Node> nodes = new ArrayList<>();

        public AsVertex(String id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return (id == null) ? 0 : id.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            AsVertex other = (AsVertex) obj;
            if (id == null) {
                return other.id == null;
            } else {
                return id.equals(other.id);
            }
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void addNode(Node node) {
            this.nodes.add(node);
        }

        public boolean containsNode(Node node) {
            return this.nodes.contains(node);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("(").append(id).append(")");
            return sb.toString();
        }

    }

    private static final String PAR_FILE = "file";

    private final Graph<AsVertex, DefaultWeightedEdge> graph;

    private Map<String, ArrayList<Node>> asToNodesMap;

    private Map<Map.Entry<Node, Node>, GraphPath<AsVertex, DefaultWeightedEdge>> pathCache = new HashMap<>();

    public GraphTopology(String prefix) {
        String path = Configuration.getString(prefix + "." + PAR_FILE);
        graph = GraphGenerator.empty();
        GraphUtils.createImporter().importGraph(graph, new File(path));

        asToNodesMap = new HashMap<String, ArrayList<Node>>();
        for (var v : graph.vertexSet()) {
            asToNodesMap.put(v.getId(), new ArrayList<Node>());
        }
    }

    @Override
    public Object clone() {
        return this;
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {}

    private AsVertex addNode(Node node) {
        AsVertex as = GraphUtils.randomVertex(graph, CommonState.r);
        as.addNode(node);
        return as;
    }

    private AsVertex asOfNode(Node node) {
        Optional<AsVertex> as = graph.vertexSet().stream().filter(
            v -> v.containsNode(node)).findAny();
        if (as.isPresent()) {
            return as.get();
        }

        return addNode(node);
    }

    private GraphPath<AsVertex, DefaultWeightedEdge> shortestPath(Node src, Node dst) {
        Map.Entry<Node, Node> cacheKey = new AbstractMap.SimpleImmutableEntry<>(src, dst);
        if (pathCache.containsKey(cacheKey)) {
            return pathCache.get(cacheKey);
        }

        AsVertex srcVertex = asOfNode(src);
        AsVertex dstVertex = asOfNode(dst);

        DijkstraShortestPath<AsVertex, DefaultWeightedEdge> dijkstra =
            new DijkstraShortestPath<>(graph);
        SingleSourcePaths<AsVertex, DefaultWeightedEdge> paths =
            dijkstra.getPaths(srcVertex);
        GraphPath<AsVertex, DefaultWeightedEdge> path = paths.getPath(dstVertex);

        pathCache.put(new AbstractMap.SimpleImmutableEntry<>(src, dst), path);

        return path;
    }

    public int getHops(Node src, Node dst) {
        return shortestPath(src, dst).getLength();
    }

    public int getLatency(Node src, Node dst) {
        return (int) shortestPath(src, dst).getWeight();
    }

}
