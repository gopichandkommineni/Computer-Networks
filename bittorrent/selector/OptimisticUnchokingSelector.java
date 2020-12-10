package bittorrent.selector;

import bittorrent.state.BitTorrentStatus;
import bittorrent.state.PeerStatus;
import bittorrent.messaging.ChokeMessage;
import bittorrent.messaging.UnchokeMessage;
import bittorrent.ops.Logger;

import java.util.*;

public class OptimisticUnchokingSelector extends TimerTask {

    private PeerStatus livePeerStatus;

    public OptimisticUnchokingSelector(PeerStatus livePeerStatus) {
        this.livePeerStatus = livePeerStatus;
    }

    @Override
    public void run() {
        System.out.println("OptimisticUnchokingTask: Begin");

        if (livePeerStatus.findPeersInterested().isEmpty()) {
            System.out.println("OptimisticUnchokingTask: No interested neighbors for " + this.livePeerStatus.getPeerId());
            return;
        }

        List<String> alreadyChokedNeighbours = new ArrayList<>();

        for (String pId: livePeerStatus.findPeersInterested().values()) {
            if (pId.equals(livePeerStatus.getPeerId())) {
                continue;
            }
            if (!livePeerStatus.findPreferPeers().containsKey(pId)) {
                alreadyChokedNeighbours.add(pId);
            }
        }
        if (alreadyChokedNeighbours.isEmpty()) {
            System.out.println("OptimisticUnchokingTask: No choked neighbors!");
            return;
        }
        Collections.shuffle(alreadyChokedNeighbours);
        String optimisticUnchokedId = alreadyChokedNeighbours.get(0);
        livePeerStatus.setCurrOptUnchPeer(optimisticUnchokedId);
        livePeerStatus.conList().get(optimisticUnchokedId).sendMessage(new UnchokeMessage());
        Logger.fetchLogger(livePeerStatus.getPeerId()).optimisticallyUnchokedNeighborChange(optimisticUnchokedId);
    }
}
