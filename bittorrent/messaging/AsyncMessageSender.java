package bittorrent.messaging;

import bittorrent.state.BitTorrentStatus;
import bittorrent.state.PeerStatus;
import bittorrent.conn.PeerConnectionManager;
import bittorrent.messaging.Message;

import java.io.BufferedInputStream;
import java.io.IOException;
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
            System.out.println("Async running "+BitTorrentStatus.findPeers().keySet().toString());
            PeerStatus peerState = BitTorrentStatus.findPeers().get(this.peerId);
            while (running.get()) {
                Message message = peerState.getBlockingPeers().take();
                System.out.println(peerId + ": Removed from queue " + message.findMsgType());

            }
        } catch (InterruptedException e) {

            // exit with a message in case of exception
            System.out.println("Ending AsyncMessageSender");
        }
    }
    public static byte[] inputBytes(BufferedInputStream ip, byte[] byteArr, int length) throws IOException {
        int size = length;
        int index = 0;
        while (size != 0) {
            int dataSize = ip.available();
            int input = Math.min(size, dataSize);
            byte[] dataInput = new byte[input];
            if (input != 0) {
                ip.read(dataInput);
                byteArr = combineByteArray(byteArr, index, dataInput, input);
                index += input;
                size -= input;
            }
        }
        return byteArr;
    }

    public static byte[] combineByteArray(byte[] i, int iLength, byte[] j, int jLength) {
        byte[] res = new byte[iLength + jLength];
        System.arraycopy(i, 0, res, 0, iLength);
        System.arraycopy(j, 0, res, iLength, jLength);
        return res;
    }

    public void stop() {
        this.running.set(false);
    }
}
