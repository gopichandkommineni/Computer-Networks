package bittorrent.handlers;

import bittorrent.dtos.PeerState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class IncomingConnectionHandler implements Runnable{

    private PeerState peerState;

    public IncomingConnectionHandler(PeerState peerState) {
        this.peerState = peerState;
    }

    @Override
    public void run() {
        // thread to accept the incoming peer connections and create new threads for connections between 2 peers
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(peerState.getPort());
            this.peerState.assignServSocket(socket);
            while (true) {
                System.out.println("Peer Id " + peerState.getPeerId() + " receiving Incoming connections");
                Socket clientSocket = socket.accept();
                System.out.println("Connection is established to " + peerState.getPort() + " from " + clientSocket.getRemoteSocketAddress());
                Thread th = new Thread(new PeerConnectionHandler(clientSocket, peerState));
                th.start();
            }
        }
        catch (Exception e) {

            System.out.println(this.peerState.getPeerId() + ": Exiting IncomingConnectionHandler!");
        }
        finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
