package peersim.kademlia;

import java.util.List;

import peersim.core.Node;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MyRoutingTable implements Cloneable {

    private List<MyKBucket> buckets;

    public BigInteger myId = null;

    public int totalReplace = 0;

    public MyRoutingTable() {
        this.buckets = new ArrayList<>();
        this.buckets.add(new MyKBucket());
    }

    private int bucketIdxForPeer(BigInteger peerId) {
        int cpl = Util.prefixLen(this.myId, peerId);
        if (cpl >= this.buckets.size()) {
            // last bucket
            return this.buckets.size() - 1;
        }
        return cpl;
    }

    private void unfold() {
        MyKBucket bucket = this.buckets.get(this.buckets.size() - 1);
        MyKBucket newBucket = bucket.split(this.buckets.size() - 1, this.myId);
        this.buckets.add(newBucket);

        if (newBucket.full()) {
            this.unfold();
        }
    }

    public void add(Node myNode, Node peerNode, BigInteger peerId) {
        int bucketIdx = this.bucketIdxForPeer(peerId);
        MyKBucket bucket = this.buckets.get(bucketIdx);

        if (peerId == myId) {
            return;
        }

        // peer already exists in routing table
        if (bucket.contains(peerId)) {
            return;
        }

        // check if enough space in bucket
        if (!bucket.full()) {
            bucket.add(myNode, peerNode, peerId);
            return;
        }

        // check if last bucket full
        if (bucketIdx == this.buckets.size() - 1) {
            this.unfold();

            bucketIdx = this.bucketIdxForPeer(peerId);
            bucket = this.buckets.get(bucketIdx);

            if (!bucket.full()) {
                bucket.add(myNode, peerNode, peerId);
                return;
            }
        }

        bucket.replace(myNode, peerNode, peerId);
        this.totalReplace++;
    }

    public void remove(BigInteger peerId) {
        int bucketIdx = this.bucketIdxForPeer(peerId);
        MyKBucket bucket = this.buckets.get(bucketIdx);

        if (bucket.contains(peerId)) {
            bucket.remove(peerId);

            // re-organize buckets
            while (true) {
                // remove last bucket if empty
                if (
                    this.buckets.size() > 1 &&
                    this.buckets.get(this.buckets.size() - 1).size() == 0
                ) {
                    this.buckets.remove(this.buckets.size() - 1);
                    continue;
                }

                // remove second-last bucket if empty
                if (
                    this.buckets.size() >= 2 &&
                    this.buckets.get(this.buckets.size() - 2).size() == 0
                )
                {
                    this.buckets.remove(this.buckets.size() - 2);
                    continue;
                }

                break;
            }
        }
    }

    public BigInteger[] closest(BigInteger key) {
        BigInteger[] result = new BigInteger[KademliaCommonConfig.K];
        List<BigInteger> candidates = new ArrayList<>();

        int cpl = Util.prefixLen(key, this.myId);
        if (cpl >= this.buckets.size()) {
            cpl = this.buckets.size() - 1;
        }

        // add all from corresponding bucket
        candidates.addAll(this.buckets.get(cpl).peerIds());

        // if less K, add from next buckets
        if (candidates.size() < KademliaCommonConfig.K) {
            for (int i = cpl + 1; i < this.buckets.size(); i++) {
                candidates.addAll(this.buckets.get(i).peerIds());
            }
        }

        // if less K, add from previous buckets
        if (candidates.size() < KademliaCommonConfig.K) {
            for (int i = cpl - 1; i >= 0; i--) {
                candidates.addAll(this.buckets.get(i).peerIds());
            }
        }

        // sort by distance
        Collections.sort(candidates, new Comparator<BigInteger>() {
            public int compare(BigInteger a, BigInteger b) {
                return Util.distance(a, key).compareTo(Util.distance(b, key));
            }
        });

        // return result
        for (int i = 0; i < KademliaCommonConfig.K && i < candidates.size(); i++) {
            result[i] = candidates.get(i);
        }
        return result;
    }

    public int numOfBuckets() {
        return this.buckets.size();
    }

    public int numOfPeers() {
        int n = 0;
        for (int i = 0; i < this.buckets.size(); i++) {
            n += this.buckets.get(i).size();
        }
        return n;
    }

}
