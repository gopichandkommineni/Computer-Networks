package com.bittorrent.scheduler;

import com.bittorrent.dtos.BitTorrentState;
import com.bittorrent.dtos.PeerState;
import com.bittorrent.messaging.ChokeMessage;
import com.bittorrent.messaging.UnchokeMessage;
import com.bittorrent.utils.Logger;

import java.util.*;

public class PreferredNeighborsScheduler extends TimerTask {
    private PeerState currentPeerState;
    private PriorityQueue<PeerState> maxHeap;

    public PreferredNeighborsScheduler(PeerState currentPeerState) {
        this.currentPeerState = currentPeerState;
        this.maxHeap = new PriorityQueue<>(BitTorrentState.numberOfPreferredNeighbors,
                new Comparator<PeerState>() {
                    @Override
                    public int compare(PeerState peerState1, PeerState peerState2) {
                        return (int) (peerState2.findDataRate() - peerState1.findDataRate());
                    }
                });
    }

    @Override
    public void run() {
        System.out.println("PreferredNeighborsTask " + this.currentPeerState.getPeerId() + ": interested neighbors - " +
                this.currentPeerState.findPeersInterested().values());

        if (currentPeerState.findPeersInterested().isEmpty()) {
            System.out.println("PreferredNeighborsTask: No interested neighbors for " + this.currentPeerState.getPeerId());
            return;
        }

        maxHeap.clear();
        for (String interestedNeighbor: currentPeerState.findPeersInterested().values()) {
            maxHeap.add(BitTorrentState.findPeers().get(interestedNeighbor));
        }
        System.out.println("PreferredNeighborsTask: maxHeapSize - "+maxHeap.size());



        Map<String, String> oldPreferredNeighbours = new HashMap<>();
        oldPreferredNeighbours.putAll(currentPeerState.findPreferPeers());

        Map<String, String> newPreferredNeighbours = new HashMap<>();
        for (int i = 0; i < BitTorrentState.findNumPreferPeers(); i++) {
            if (maxHeap.size() > 0) {
                String peerId = maxHeap.poll().getPeerId();
                if (currentPeerState.getPeerId().equals(peerId)) {
                    // this should not happen
                    i--;
                    continue;
                }
                newPreferredNeighbours.put(peerId, peerId);
                if (!oldPreferredNeighbours.containsKey(peerId)) {
                    //System.out.println(this.currentPeerId + ": sending UNCHOKE to "+peerId);
                    if (currentPeerState.conList().size() > 0) {
                        currentPeerState.conList().get(peerId).sendMessage(new UnchokeMessage());
                    }
                }
            }
            else {
                System.out.println("maxHeap empty");
            }
        }
        for (String peerId: oldPreferredNeighbours.values()) {
            if (currentPeerState.getCurrOptUnchPeer() != null &&
                currentPeerState.getCurrOptUnchPeer().equals(peerId)) {
                continue;
            }
            if (!newPreferredNeighbours.containsKey(peerId)) {
                //System.out.println(this.currentPeerId + ": sending CHOKE to "+peerId);
                if (currentPeerState.conList().size() > 0) {
                    currentPeerState.conList().get(peerId).sendMessage(new ChokeMessage());
                }
            }
        }
        currentPeerState.findPreferPeers().clear();
        currentPeerState.findPreferPeers().putAll(newPreferredNeighbours);
        Logger.fetchLogger(this.currentPeerState.getPeerId()).preferredNeighborsChange(newPreferredNeighbours);
    }
}
