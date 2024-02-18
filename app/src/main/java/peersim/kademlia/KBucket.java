package peersim.kademlia;

import java.math.BigInteger;
import java.util.TreeMap;
import java.util.Map;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;

import peersim.jgrapht.GraphTopology;

/**
 * This class implements a kademlia k-bucket. Function for the management of the neighbours update are also implemented
 *
 * @author Daniele Furlan, Maurizio Bonani
 * @version 1.0
 */
public class KBucket implements Cloneable {

	/** Maps node id to add time */
	protected TreeMap<BigInteger, Long> neighbours = null;
	/** Maps node id to number of hops */
	protected TreeMap<BigInteger, Integer> hops = null;

	private final int topology;

	// empty costructor
	public KBucket() {
		neighbours = new TreeMap<BigInteger, Long>();
		hops = new TreeMap<BigInteger, Integer>();

		// TODO: not the finest way to do this...
		topology = Configuration.getPid("protocol.1grapht.topo");
	}

	// add a neighbour to this k-bucket
	public void addNeighbour(Node me, Node neighbour, BigInteger neighborNodeId) {
		// determine number of hops
		int neighbourHops = ((GraphTopology) me.getProtocol(topology)).getHops(me, neighbour);

		long time = CommonState.getTime();
		if (neighbours.size() < KademliaCommonConfig.K) { // k-bucket isn't full
			neighbours.put(neighborNodeId, time); // add neighbour to the tail of the list
			hops.put(neighborNodeId, neighbourHops);
		} else {
			// bucket is full, determine which node to evict

			// search for node with max hops
			BigInteger maxKey = neighbours.firstKey();
			int maxHops = hops.get(maxKey);
			for (Map.Entry<BigInteger, Long> entry : neighbours.entrySet()) {
				if (hops.get(entry.getKey()) > maxHops) {
					maxKey = entry.getKey();
					maxHops = hops.get(maxKey);
				}
			}

			BigInteger toEvict;
			if (KademliaCommonConfig.SORT == 0) {
				// evict first or...
				toEvict = neighbours.firstKey();
			} else {
				// evict most hops
				toEvict = maxKey;
			}

			// evict node
			neighbours.remove(toEvict);
			hops.remove(toEvict);
			// insert new node
			neighbours.put(neighborNodeId, time);
			hops.put(neighborNodeId, neighbourHops);
		}
	}

	// remove a neighbour from this k-bucket
	public void removeNeighbour(BigInteger node) {
		neighbours.remove(node);
		hops.remove(node);
	}

	public Object clone() {
		KBucket dolly = new KBucket();
		for (BigInteger node : neighbours.keySet()) {
			dolly.neighbours.put(new BigInteger(node.toByteArray()), 0l);
			dolly.hops.put(new BigInteger(node.toByteArray()), hops.get(node));
		}
		return dolly;
	}

	public String toString() {
		String res = "{\n";

		for (BigInteger node : neighbours.keySet()) {
			res += node + "\n";
		}

		return res + "}";
	}
}
