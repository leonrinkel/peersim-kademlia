package peersim.kademlia;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.jgrapht.GraphTopology;

public class MyKBucket {

    private List<BigInteger> _peerIds;
    private List<Integer> _peerHops;
    private List<Integer> _peerLatencies;

    private final int _topologyPid;

    public MyKBucket() {
        this._peerIds = new ArrayList<>();
        this._peerHops = new ArrayList<>();
        this._peerLatencies = new ArrayList<>();

        this._topologyPid = Configuration.getPid("protocol.1grapht.topo");
    }

    public int size() {
        return this._peerIds.size();
    }

    public boolean contains(BigInteger peerId) {
        return this._peerIds.contains(peerId);
    }

    public boolean full() {
        return this.size() >= KademliaCommonConfig.K;
    }

    public void add(Node myNode, Node peerNode, BigInteger peerId) {
        // determine number of hops to peer
        int hops = ((GraphTopology) myNode
            .getProtocol(this._topologyPid))
            .getHops(myNode, peerNode);
        // determine latency to peer
        int latency = ((GraphTopology) myNode
            .getProtocol(this._topologyPid))
            .getLatency(myNode, peerNode);

        // add peer
        this._peerIds.addFirst(peerId);
        this._peerHops.addFirst(hops);
        this._peerLatencies.addFirst(latency);
    }

    public void remove(BigInteger peerId) {
        int index = this._peerIds.indexOf(peerId);
        this._peerIds.remove(index);
        this._peerHops.remove(index);
        this._peerLatencies.remove(index);
    }

    public void replace(Node myNode, Node peerNode, BigInteger peerId) {
        // determine which node to evict, either just last, or...
        int indexToEvict = this._peerIds.size() - 1;
        if (KademliaCommonConfig.SORT == 1) {
            // ...with path length sorting, search for node with max hops
            int maxHops = Integer.MIN_VALUE;
            for (int i = 0; i < this._peerIds.size(); i++) {
                if (this._peerHops.get(i) > maxHops) {
                    indexToEvict = i;
                    maxHops = this._peerHops.get(i);
                }
            }
        } else if (KademliaCommonConfig.SORT == 2) {
            // ...with latency sorting, search for node with max latency
            int maxLatency = Integer.MIN_VALUE;
            for (int i = 0; i < this._peerIds.size(); i++) {
                if (this._peerLatencies.get(i) > maxLatency) {
                    indexToEvict = i;
                    maxLatency = this._peerLatencies.get(i);
                }
            }
        }

        this._peerIds.remove(indexToEvict);
        this._peerHops.remove(indexToEvict);
        this._peerLatencies.remove(indexToEvict);

        this.add(myNode, peerNode, peerId);
    }

    public MyKBucket split(int cpl, BigInteger myId) {
        MyKBucket newBucket = new MyKBucket();

        List<BigInteger> peersToMove = new ArrayList<>();
        for (BigInteger peerId : this._peerIds) {
            if (Util.prefixLen(myId, peerId) > cpl) {
                peersToMove.add(peerId);
            }
        }

        for (BigInteger peerId : peersToMove) {
            int index = this._peerIds.indexOf(peerId);
            int peerHops = this._peerHops.get(index);
            int peerLatency = this._peerLatencies.get(index);
            newBucket._peerIds.add(peerId);
            newBucket._peerHops.add(peerHops);
            newBucket._peerLatencies.add(peerLatency);

            this.remove(peerId);
        }

        return newBucket;
    }

    public List<BigInteger> peerIds() {
        return this._peerIds;
    }

}
