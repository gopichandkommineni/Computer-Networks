package com.bittorrent.scheduler;

import com.bittorrent.dtos.BitTorrentState;
import com.bittorrent.dtos.PeerState;
import com.bittorrent.messaging.ChokeMessage;
import com.bittorrent.messaging.UnchokeMessage;
import com.bittorrent.utils.Logger;

import java.util.*;

public class OptimisticUnchokingScheduler extends TimerTask {

    private PeerState currentPeerState;

    public OptimisticUnchokingScheduler(PeerState currentPeerState) {
        this.currentPeerState = currentPeerState;
    }

    @Override
    public void run() {
        System.out.println("OptimisticUnchokingTask: start");

        if (currentPeerState.findPeersInterested().isEmpty()) {
            System.out.println("OptimisticUnchokingTask: No interested neighbors for " + this.currentPeerState.getPeerId());
            return;
        }

        List<String> chokedNeighbours = new ArrayList<>();

        for (String peerId: currentPeerState.findPeersInterested().values()) {
            if (peerId.equals(currentPeerState.getPeerId())) {
                continue;
            }
            if (!currentPeerState.findPreferPeers().containsKey(peerId)) {
                chokedNeighbours.add(peerId);
            }
        }
        if (chokedNeighbours.isEmpty()) {
            System.out.println("OptimisticUnchokingTask: No choked neighbors!");
            return;
        }
        Collections.shuffle(chokedNeighbours);
        String optimisticUnchokedPeerId = chokedNeighbours.get(0);
        currentPeerState.setCurrOptUnchPeer(optimisticUnchokedPeerId);
        currentPeerState.conList().get(optimisticUnchokedPeerId).sendMessage(new UnchokeMessage());
        Logger.fetchLogger(currentPeerState.getPeerId()).optimisticallyUnchokedNeighborChange(optimisticUnchokedPeerId);
    }
}
