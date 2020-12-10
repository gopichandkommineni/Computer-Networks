package bittorrent.scheduler;

import bittorrent.state.BitTorrentState;
import bittorrent.state.PeerState;
import bittorrent.messaging.ChokeMessage;
import bittorrent.messaging.UnchokeMessage;
import bittorrent.utils.Logger;

import java.util.*;

public class PreferredNeighborsScheduler extends TimerTask {
    private PeerState livePeerState;
    private PriorityQueue<PeerState> maxHeap;

    public PreferredNeighborsScheduler(PeerState livePeerState) {
        this.livePeerState = livePeerState;
        this.maxHeap = new PriorityQueue<>(BitTorrentState.numberOfPreferredNeighbors,
                new Comparator<PeerState>() {
                    @Override
                    public int compare(PeerState ps1, PeerState ps2) {
                        return (int) (ps2.findDataRate() - ps1.findDataRate());
                    }
                });
    }

    @Override
    public void run() {
        System.out.println("PreferredNeighborsTask " + this.livePeerState.getPeerId() + ": interested neighbors - " +
                this.livePeerState.findPeersInterested().values());

        if (livePeerState.findPeersInterested().isEmpty()) {
            System.out.println("PreferredNeighborsTask: No interested neighbors for " + this.livePeerState.getPeerId());
            return;
        }

        maxHeap.clear();
        for (String interestedPeer: livePeerState.findPeersInterested().values()) {
            maxHeap.add(BitTorrentState.findPeers().get(interestedPeer));
        }
        System.out.println("PreferredNeighborsTask: maxHeapSize - "+maxHeap.size());



        Map<String, String> previousPreferredNeighbours = new HashMap<>();
        previousPreferredNeighbours.putAll(livePeerState.findPreferPeers());

        Map<String, String> nextPreferredNeighbours = new HashMap<>();
        for (int j = 0; j < BitTorrentState.findNumPreferPeers(); j++) {
            if (maxHeap.size() > 0) {
                String pId = maxHeap.poll().getPeerId();
                if (livePeerState.getPeerId().equals(pId)) {
                    // this should not happen
                    j--;
                    continue;
                }
                nextPreferredNeighbours.put(pId, pId);
                if (!previousPreferredNeighbours.containsKey(pId)) {
                    //System.out.println(this.currentPeerId + ": sending UNCHOKE to "+pId);
                    if (livePeerState.conList().size() > 0) {
                        livePeerState.conList().get(pId).sendMessage(new UnchokeMessage());
                    }
                }
            }
            else {
                System.out.println("maxHeap empty");
            }
        }
        for (String pId: previousPreferredNeighbours.values()) {
            if (livePeerState.getCurrOptUnchPeer() != null &&
                livePeerState.getCurrOptUnchPeer().equals(pId)) {
                continue;
            }
            if (!nextPreferredNeighbours.containsKey(pId)) {
                //System.out.println(this.currentPeerId + ": sending CHOKE to "+pId);
                if (livePeerState.conList().size() > 0) {
                    livePeerState.conList().get(pId).sendMessage(new ChokeMessage());
                }
            }
        }
        livePeerState.findPreferPeers().clear();
        livePeerState.findPreferPeers().putAll(nextPreferredNeighbours);
        Logger.fetchLogger(this.livePeerState.getPeerId()).preferredNeighborsChange(nextPreferredNeighbours);
    }
}
