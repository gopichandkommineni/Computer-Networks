package bittorrent.scheduler;

import bittorrent.dtos.BitTorrentState;
import bittorrent.dtos.PeerState;
import bittorrent.messaging.ChokeMessage;
import bittorrent.messaging.UnchokeMessage;
import bittorrent.utils.Logger;

import java.util.*;

public class OptimisticUnchokingScheduler extends TimerTask {

    private PeerState livePeerState;

    public OptimisticUnchokingScheduler(PeerState livePeerState) {
        this.livePeerState = livePeerState;
    }

    @Override
    public void run() {
        System.out.println("OptimisticUnchokingTask: Begin");

        if (livePeerState.findPeersInterested().isEmpty()) {
            System.out.println("OptimisticUnchokingTask: No interested neighbors for " + this.livePeerState.getPeerId());
            return;
        }

        List<String> alreadyChokedNeighbours = new ArrayList<>();

        for (String pId: livePeerState.findPeersInterested().values()) {
            if (pId.equals(livePeerState.getPeerId())) {
                continue;
            }
            if (!livePeerState.findPreferPeers().containsKey(pId)) {
                alreadyChokedNeighbours.add(pId);
            }
        }
        if (alreadyChokedNeighbours.isEmpty()) {
            System.out.println("OptimisticUnchokingTask: No choked neighbors!");
            return;
        }
        Collections.shuffle(alreadyChokedNeighbours);
        String optimisticUnchokedId = alreadyChokedNeighbours.get(0);
        livePeerState.setCurrOptUnchPeer(optimisticUnchokedId);
        livePeerState.conList().get(optimisticUnchokedId).sendMessage(new UnchokeMessage());
        Logger.fetchLogger(livePeerState.getPeerId()).optimisticallyUnchokedNeighborChange(optimisticUnchokedId);
    }
}
