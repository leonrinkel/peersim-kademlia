package peersim.kademlia;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class MyKBucket {

    private List<BigInteger> _peerIds;

    public MyKBucket() {
        this._peerIds = new ArrayList<>();
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

    public void add(BigInteger peerId) {
        this._peerIds.addFirst(peerId);
    }

    public void remove(BigInteger peerId) {
        this._peerIds.remove(peerId);
    }

    public void replace(BigInteger peerId) {
        this._peerIds.removeLast();
        this._peerIds.add(peerId);
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
            newBucket.add(peerId);
            this.remove(peerId);
        }

        return newBucket;
    }

    public List<BigInteger> peerIds() {
        return this._peerIds;
    }

}
