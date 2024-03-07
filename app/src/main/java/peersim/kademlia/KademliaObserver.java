package peersim.kademlia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.util.IncrementalStats;

/**
 * This class implements a simple observer of search time and hop average in finding a node in the network
 *
 * @author Daniele Furlan, Maurizio Bonani
 * @version 1.0
 */
public class KademliaObserver implements Control {

	/**
	 * keep statistics of the number of hops of every message delivered.
	 */
	public static IncrementalStats hopStore = new IncrementalStats();

	/**
	 * keep statistics of the time every message delivered.
	 */
	public static IncrementalStats timeStore = new IncrementalStats();

	/**
	 * keep statistic of number of message delivered
	 */
	public static IncrementalStats msg_deliv = new IncrementalStats();

	/**
	 * keep statistic of number of find operation
	 */
	public static IncrementalStats find_op = new IncrementalStats();

	public static IncrementalStats dist0count = new IncrementalStats();
	public static IncrementalStats dist1count = new IncrementalStats();
	public static IncrementalStats dist2count = new IncrementalStats();
	public static IncrementalStats dist3count = new IncrementalStats();
	public static IncrementalStats dist4count = new IncrementalStats();

	/** Parameter of the protocol we want to observe */
	private static final String PAR_PROT = "protocol";

	/** Protocol id */
	private int pid;

	/** Prefix to be printed in output */
	private String prefix;

	public KademliaObserver(String prefix) {
		this.prefix = prefix;
		pid = Configuration.getPid(prefix + "." + PAR_PROT);
	}

	/**
	 * print the statistical snapshot of the current situation
	 *
	 * @return boolean always false
	 */
	public boolean execute() {
		// get the real network size
		int sz = Network.size();
		for (int i = 0; i < Network.size(); i++)
			if (!Network.get(i).isUp())
				sz--;

		String s = String.format("[time=%d]:[N=%d current nodes UP] [D=%f msg deliv] [%f min h] [%f average h] [%f max h] [%d min l] [%d msec average l] [%d max l]", CommonState.getTime(), sz, msg_deliv.getSum(), hopStore.getMin(), hopStore.getAverage(), hopStore.getMax(), (int) timeStore.getMin(), (int) timeStore.getAverage(), (int) timeStore.getMax());

		System.err.println(String.format(
			"[time=%d]:[dist0count=%f] [dist1count=%f] [dist2count=%f] [dist3count=%f] [dist4count=%f]",
			CommonState.getTime(),
			dist0count.getSum(),
			dist1count.getSum(),
			dist2count.getSum(),
			dist3count.getSum(),
			dist4count.getSum()
		));

		int sumOfBuckets = 0;
		int sumOfPeers = 0;
		int minOfBuckets = Integer.MAX_VALUE, maxOfBuckets = Integer.MIN_VALUE;
		int minOfPeers = Integer.MAX_VALUE, maxOfPeers = Integer.MIN_VALUE;
		for (int i = 0; i < Network.size(); i++)
		{
			MyRoutingTable table =
				((KademliaProtocol) Network.get(i).getProtocol(this.pid)).routingTable;

				sumOfBuckets += table.numOfBuckets();
			sumOfPeers += table.numOfPeers();

			if (table.numOfBuckets() < minOfBuckets)
				minOfBuckets = table.numOfBuckets();
			if (table.numOfBuckets() > maxOfBuckets)
				maxOfBuckets = table.numOfBuckets();

			if (table.numOfPeers() < minOfPeers)
				minOfPeers = table.numOfPeers();
			if (table.numOfPeers() > maxOfPeers)
				maxOfPeers = table.numOfPeers();
		}
		double avgNumOfBuckets = ((double) sumOfBuckets) / Network.size();
		double avgNumOfPeers = ((double) sumOfPeers) / Network.size();

		System.err.println(String.format(
			"[time=%d]:[%d min buckets] [%f avg buckets] [%d max buckets] [%d min peers] [%f avg peers] [%d max peers]",
			CommonState.getTime(),
			minOfBuckets, avgNumOfBuckets, maxOfBuckets,
			minOfPeers, avgNumOfPeers, maxOfPeers
		));

		if (CommonState.getTime() == 3600000) {
			// create hop file
			try {
				File f = new File("D:/simulazioni/hopcountNEW.dat"); // " + sz + "
				f.createNewFile();
				BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
				out.write(String.valueOf(hopStore.getAverage()).replace(".", ",") + ";\n");
				out.close();
			} catch (IOException e) {
			}
			// create latency file
			try {
				File f = new File("D:/simulazioni/latencyNEW.dat");
				f.createNewFile();
				BufferedWriter out = new BufferedWriter(new FileWriter(f, true));
				out.write(String.valueOf(timeStore.getAverage()).replace(".", ",") + ";\n");
				out.close();
			} catch (IOException e) {
			}

		}

		System.err.println(s);

		return false;
	}
}
