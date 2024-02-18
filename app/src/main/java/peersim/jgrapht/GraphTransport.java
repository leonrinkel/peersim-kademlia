package peersim.jgrapht;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

public class GraphTransport implements Transport {

    private static final String PAR_TOPO = "topo";

    private final int topoPid;

    public GraphTransport(String prefix) {
        topoPid = Configuration.getPid(prefix + "." + PAR_TOPO);
    }

    @Override
    public Object clone() {
        return this;
    }

    @Override
    public void send(Node src, Node dest, Object msg, int pid) {
        long latency = getLatency(src, dest);
        EDSimulator.add(latency, msg, dest, pid);
    }

    @Override
    public long getLatency(Node src, Node dest) {
        int totalLatency =
            0 + // TODO: intra-AS latency
            ((GraphTopology) src.getProtocol(topoPid)).getLatency(src, dest);
        return totalLatency;
    }

}
