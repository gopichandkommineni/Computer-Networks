package bittorrent.messaging;

import bittorrent.state.BitTorrentStatus;
import bittorrent.state.PeerStatus;
import bittorrent.conn.PeerConnectionManager;
import bittorrent.messaging.Message;

import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncMessageSender implements Runnable {

    private String peerId;
    private AtomicBoolean running = new AtomicBoolean(false);

    public AsyncMessageSender(String peerId){
        this.peerId = peerId;
    }

    @Override
    public void run() {
        try {
            running.set(true);
            System.out.println("Async running "+BitTorrentState.findPeers().keySet().toString());
            PeerStatus peerState = BitTorrentState.findPeers().get(this.peerId);
            while (running.get()) {
                Message message = peerState.getBlockingPeers().take();
                System.out.println(peerId + ": Removed from queue " + message.getMessageType());
                //peerConnectionHandler.sendMessage(message);
            }
        } catch (InterruptedException e) {
            System.out.println("Ending AsyncMessageSender");
        }
    }

    public void stop() {
        this.running.set(false);
    }
}
