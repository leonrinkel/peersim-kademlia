package peersim.jgrapht;

import org.jgrapht.Graph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.AttributeType;
import org.jgrapht.nio.DefaultAttribute;
import org.jgrapht.nio.GraphExporter;
import org.jgrapht.nio.GraphImporter;
import org.jgrapht.nio.IntegerIdProvider;
import org.jgrapht.nio.graphml.GraphMLExporter;
import org.jgrapht.nio.graphml.GraphMLImporter;
import org.jgrapht.nio.graphml.GraphMLExporter.AttributeCategory;

import peersim.jgrapht.GraphTopology.AsVertex;
import peersim.jgrapht.GraphTopology.LinkEdge;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.function.Supplier;

public class GraphUtils {

    static class AsVertexSupplier implements Supplier<AsVertex> {

        private int id = 0;

        @Override
        public AsVertex get() {
            return new AsVertex(String.valueOf(id++));
        }

    }

    static class LinkEdgeSupplier implements Supplier<LinkEdge> {

        @Override
        public LinkEdge get() {
            return new LinkEdge();
        }

    }

    static GraphImporter<AsVertex, LinkEdge> createImporter() {
        GraphMLImporter<AsVertex, LinkEdge> importer =
            new GraphMLImporter<>();

        importer.addEdgeAttributeConsumer((p, attrValue) -> {
            LinkEdge e = p.getFirst();
            String attrName = p.getSecond();

            if (attrName.equals("latency")) {
                e.setLatency(Integer.parseInt(attrValue.getValue()));
            }
        });

        return importer;
    }

    static GraphExporter<AsVertex, LinkEdge> createExporter() {
        GraphMLExporter<AsVertex, LinkEdge> exporter =
            new GraphMLExporter<>(v -> v.getId());

        exporter.setEdgeIdProvider(
            new IntegerIdProvider<LinkEdge>(0));

        exporter.registerAttribute("latency", AttributeCategory.EDGE, AttributeType.INT);
        exporter.setEdgeAttributeProvider(e -> {
            Map<String, Attribute> m = new HashMap<>();
            if (e.getLatency() != null) {
                m.put("latency", DefaultAttribute.createAttribute(e.getLatency()));
            }
            return m;
        });

        return exporter;
    }

    static AsVertex randomVertex(Graph<AsVertex, LinkEdge> graph, Random r) {
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
