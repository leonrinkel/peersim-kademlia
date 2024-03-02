package peersim.kademlia;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.jgrapht.GraphTopology;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;

/**
 * This control generates random search traffic from nodes to random destination node.
 *
 * @author Daniele Furlan, Maurizio Bonani
 * @version 1.0
 */

// ______________________________________________________________________________________________
public class TrafficGenerator implements Control {

	// ______________________________________________________________________________________________
	/**
	 * MSPastry Protocol to act
	 */
	private final static String PAR_PROT = "protocol";
	private static final String PAR_TOPO = "topo";
	private final static String PAR_PDIST = "pdist";

	/**
	 * MSPastry Protocol ID to act
	 */
	private final int pid;
	private final int topoPid;
	private final Map<Integer, Double> distProbMap = new HashMap<>();

	// ______________________________________________________________________________________________
	public TrafficGenerator(String prefix) {
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
		topoPid = Configuration.getPid(prefix + "." + PAR_TOPO);

		String[] names = Configuration.getNames(prefix + "." + PAR_PDIST);
		for (String name : names) {
			int dist = Integer.parseInt(name.split("\\.")[3]);
			double prob = Configuration.getDouble(name);
			distProbMap.put(dist, prob);
		}
	}

	private int pickDistance(Random r) {
		List<Integer> distances = new ArrayList<>(distProbMap.keySet());
		List<Double> cumulativeProbs = new ArrayList<>();
		double sum = 0;

		for (Integer dist : distances) {
			sum += distProbMap.get(dist);
			cumulativeProbs.add(sum);
		}

		double p = r.nextDouble();
		return distances.stream()
			.filter(d -> p < cumulativeProbs.get(distances.indexOf(d)))
			.findFirst().get();
	}

	// ______________________________________________________________________________________________
	/**
	 * every call of this control generates and send a random find node message
	 *
	 * @return boolean
	 */
	public boolean execute() {
		// find source node
		Node src;
		do {
			src = Network.get(CommonState.r.nextInt(Network.size()));
		} while ((src == null) || (!src.isUp()));

		// find destination node
		int desiredDistance = pickDistance(CommonState.r);
		int actualDistance;
		Node dst;
		do {
			dst = Network.get(CommonState.r.nextInt(Network.size()));
			actualDistance =
				((GraphTopology) src.getProtocol(topoPid)).getHops(src, dst);
		} while (
			(dst == null) ||
			(!dst.isUp()) ||
			(actualDistance != desiredDistance)
		);

		switch (actualDistance) {
			case 0: KademliaObserver.dist0count.add(1); break;
			case 1: KademliaObserver.dist1count.add(1); break;
			case 2: KademliaObserver.dist2count.add(1); break;
			case 3: KademliaObserver.dist3count.add(1); break;
			case 4: KademliaObserver.dist4count.add(1); break;
		}

		// send message
		Message m = Message.makeFindNode("Automatically Generated Traffic");
		m.timestamp = CommonState.getTime();
		m.dest = ((KademliaProtocol) (dst.getProtocol(pid))).nodeId;
		EDSimulator.add(0, m, src, pid);

		return false;
	}

	// ______________________________________________________________________________________________

} // End of class
// ______________________________________________________________________________________________
