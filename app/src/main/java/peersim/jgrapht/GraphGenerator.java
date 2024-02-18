package peersim.jgrapht;

import org.jgrapht.Graph;
import org.jgrapht.generate.RandomRegularGraphGenerator;
import org.jgrapht.graph.builder.GraphTypeBuilder;

import peersim.jgrapht.GraphTopology.AsVertex;
import peersim.jgrapht.GraphTopology.LinkEdge;
import peersim.jgrapht.GraphUtils.AsVertexSupplier;
import peersim.jgrapht.GraphUtils.LinkEdgeSupplier;

import java.util.Random;
import java.io.File;

public class GraphGenerator {

    static Graph<AsVertex, LinkEdge> empty() {
        Graph<AsVertex, LinkEdge> graph = GraphTypeBuilder
            .undirected()
            .weighted(false)
            .allowingMultipleEdges(false)
            .allowingSelfLoops(false)
            .vertexSupplier(new AsVertexSupplier())
            .edgeSupplier(new LinkEdgeSupplier())
            .buildGraph();
        return graph;
    }

    private static int sampleLatency(int min, int max, Random r) {
        int range = max - min + 1;
        int latency = (range == 1 ? min : min + r.nextInt(range));
        return latency;
    }

    static Graph<AsVertex, LinkEdge> with(int nodes, int degree,
        int minLatency, int maxLatency, Random r) {
        Graph<AsVertex, LinkEdge> graph = empty();

        RandomRegularGraphGenerator<AsVertex, LinkEdge> generator =
            new RandomRegularGraphGenerator<>(nodes, degree, r);
        generator.generateGraph(graph);

        for (var e : graph.edgeSet()) {
            e.setLatency(sampleLatency(minLatency, maxLatency, r));
        }

        return graph;
    }

    public static void main(String[] args) {
        File file = new File(args[0]);
        int nodes = Integer.parseInt(args[1]);
        int degree = Integer.parseInt(args[2]);
        int minLatency = Integer.parseInt(args[3]);
        int maxLatency = Integer.parseInt(args[4]);

        GraphUtils.createExporter().exportGraph(
            with(nodes, degree, minLatency, maxLatency, new Random()), file);
    }

}
