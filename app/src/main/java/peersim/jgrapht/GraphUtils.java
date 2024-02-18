package peersim.jgrapht;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.nio.GraphExporter;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.IntegerIdProvider;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.graphml.GraphMLImporter;

import peersim.jgrapht.GraphTopology.AsVertex;

import java.util.Random;
import java.util.function.Supplier;

public class GraphUtils {

    static class AsVertexSupplier implements Supplier<AsVertex> {

        private int id = 0;

        @Override
        public AsVertex get() {
            return new AsVertex(String.valueOf(id++));
        }

    }

    static GraphImporter<AsVertex, DefaultWeightedEdge> createImporter() {
        GraphMLImporter<AsVertex, DefaultWeightedEdge> importer =
            new GraphMLImporter<>();
        return importer;
    }

    static GraphExporter<AsVertex, DefaultWeightedEdge> createExporter() {
        GraphMLExporter<AsVertex, DefaultWeightedEdge> exporter =
            new GraphMLExporter<>(v -> v.getId());
        exporter.setExportEdgeWeights(true);
        exporter.setEdgeIdProvider(
            new IntegerIdProvider<DefaultWeightedEdge>(0));
        return exporter;
    }

    static AsVertex randomVertex(Graph<AsVertex, DefaultWeightedEdge> graph, Random r) {
        int size = graph.vertexSet().size();
        int item = r.nextInt(size);

        int i = 0;
        for (var v : graph.vertexSet()) {
            if (i == item) {
                return v;
            }
            i++;
        }

        return null;
    }

}
