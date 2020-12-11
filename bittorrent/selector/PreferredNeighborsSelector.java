package bittorrent.selector;

import bittorrent.state.BitTorrentStatus;
import bittorrent.state.PeerStatus;
import bittorrent.messaging.ChokeMessage;
import bittorrent.messaging.UnchokeMessage;
import bittorrent.ops.Logger;

import java.util.*;

public class PreferredNeighborsSelector extends TimerTask {
    private PeerStatus livePeerStatus;
    private PriorityQueue<PeerStatus> maxHeap;

    //constructor
    public PreferredNeighborsSelector(PeerStatus livePeerStatus) {
        this.livePeerStatus = livePeerStatus;
        this.maxHeap = new PriorityQueue<>(BitTorrentStatus.numberOfPreferredNeighbors,
                new Comparator<PeerStatus>() {
                    @Override
                    public int compare(PeerStatus ps1, PeerStatus ps2) {
                        return (int) (ps2.findDataRate() - ps1.findDataRate());
                    }
                });
    }

    @Override
    public void run() {
        System.out.println("PreferredNeighborsTask " + this.livePeerStatus.getPeerId() + ": interested neighbors - " +
                this.livePeerStatus.findPeersInterested().values());

        if (livePeerStatus.findPeersInterested().isEmpty()) {
            System.out.println("PreferredNeighborsTask: No interested neighbors for " + this.livePeerStatus.getPeerId());
            return;
        }

        maxHeap.clear();
        for (String interestedPeer: livePeerStatus.findPeersInterested().values()) {
            maxHeap.add(BitTorrentStatus.findPeers().get(interestedPeer));
        }
        System.out.println("PreferredNeighborsTask: maxHeapSize - "+maxHeap.size());


        Map<String, String> previousPreferredNeighbours = new HashMap<>();
        previousPreferredNeighbours.putAll(livePeerStatus.findPreferPeers());

        Map<String, String> nextPreferredNeighbours = new HashMap<>();
        for (int j = 0; j < BitTorrentStatus.findNumPreferPeers(); j++) {
            if (maxHeap.size() > 0) {
                String pId = maxHeap.poll().getPeerId();
                if (livePeerStatus.getPeerId().equals(pId)) {
                    j--;
                    continue;
                }
                nextPreferredNeighbours.put(pId, pId);
                if (!previousPreferredNeighbours.containsKey(pId)) {

                    if (livePeerStatus.conList().size() > 0) {
                        livePeerStatus.conList().get(pId).sendMessage(new UnchokeMessage());
                    }
                }
            }
            else {
                // heap is empty
                System.out.println("maxHeap empty");
            }
        }
        for (String pId: previousPreferredNeighbours.values()) {
            if (livePeerStatus.getCurrOptUnchPeer() != null &&
                livePeerStatus.getCurrOptUnchPeer().equals(pId)) {
                continue;
            }
            if (!nextPreferredNeighbours.containsKey(pId)) {

                if (livePeerStatus.conList().size() > 0) {
                    livePeerStatus.conList().get(pId).sendMessage(new ChokeMessage());
                }
            }
        }
        livePeerStatus.findPreferPeers().clear();
        livePeerStatus.findPreferPeers().putAll(nextPreferredNeighbours);
        Logger.fetchLogger(this.livePeerStatus.getPeerId()).preferredNeighborsChange(nextPreferredNeighbours);
    }
}
