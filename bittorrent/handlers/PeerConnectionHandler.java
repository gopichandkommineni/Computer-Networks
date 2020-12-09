package bittorrent.handlers;

import bittorrent.dtos.BitTorrentState;
import bittorrent.dtos.PeerState;
import bittorrent.messaging.*;
import bittorrent.utils.FileUtils;
import bittorrent.utils.Logger;

import java.io.*;
import java.net.Socket;
import java.util.BitSet;
import java.util.Map;

public class PeerConnectionHandler implements Runnable{

    private Socket peerSocket = null;
    private PeerState peerState = null;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private Logger logger;
    private String remotePeerId;
    private long startTime;
    private long stopTime;
    private boolean running = false;
    private Thread asyncMessageSender;

    public PeerConnectionHandler(Socket peerSocket, PeerState peerState) {
        this.peerSocket = peerSocket;
        this.peerState = peerState;
        this.logger = Logger.fetchLogger(peerState.getPeerId());
    }

    public void assignRemotePeerId(String remotePeerId) {
        // assign remote peerId to Peer
        this.remotePeerId = remotePeerId;
    }

    @Override
    public void run() {
        try
        {
            running = true;
            os = new ObjectOutputStream(peerSocket.getOutputStream());

            // send new handshake message to new connection 
            sendMessage(new HandshakeMessage(this.peerState.getPeerId()));

            Message receivedMsg = null;
            while (running) {
                receivedMsg = receiveMessage();
                // received message from peer
                System.out.println(this.peerState.getPeerId() + ": Received message type: " +
                        receivedMsg.findMsgType().name() + " from " + this.remotePeerId + ", message: " +
                        receivedMsg.toString());

                switch (receivedMsg.findMsgType()) {
                    case HANDSHAKE: {
                        processHandShakeMessage(receivedMsg);
                        break;
                    }
                    case BITFIELD: {
                        processBitField(receivedMsg);
                        break;
                    }
                    case INTERESTED: {
                        processInterestedMessage();
                        break;
                    }
                    case NOT_INTERESTED: {
                        processNotInterestedMessage();
                        break;
                    }
                    case REQUEST: {
                        processPeerRequest(receivedMsg);
                        break;
                    }
                    case PIECE: {
                        processPiece(receivedMsg);
                        break;
                    }
                    case HAVE: {
                        processHaveMessage(receivedMsg);
                        break;
                    }
                    case CHOKE: {
                        processChokeMessage();
                        break;
                    }
                    case UNCHOKE: {
                        processUnChokeMessage();
                        break;
                    }
                    default:
                        System.out.println("Not implemented!");
                }
            }
        }
        catch (Exception ex)
        {
            System.out.println(this.peerState.getPeerId() + ": Exiting PeerConnectionHandler because of " + ex.getStackTrace()[0]);
            stop();
        }
    }

    private void processChokeMessage() {
        // if it is chocke message log it
        logger.choked(remotePeerId);
    }

    private void processUnChokeMessage() {
        // write message to log file and process the next interested piece
        logger.unchoked(remotePeerId);

        int interestingPieceIndex = getNextInterestingPieceIndex(BitTorrentState.findPeers().get(remotePeerId).getBitField(), this.peerState.getBitField());

        if (interestingPieceIndex != -1) {
            RequestMessage requestMessage = new RequestMessage(interestingPieceIndex);
            sendMessage(requestMessage);
        }
    }

    private void processHaveMessage(Message receivedMsg) {

        // if the peer have the piece send interested message 
        HaveMessage haveMessage = (HaveMessage) receivedMsg;
        int index = (int) haveMessage.findPayLoad();

        logger.receivedHaveMsg(remotePeerId, index);

        // set peer bitset info
        BitTorrentState.findPeers().get(remotePeerId).getBitField().set(index);

        if (!this.peerState.getBitField().get(index)) {
            System.out.println(this.peerState.getPeerId() + "Sending interested message for piece" + index);
            sendMessage(new InterestedMessage());
            RequestMessage requestMessage = new RequestMessage(index);
            sendMessage(requestMessage);
        }
    }

    private void processPiece(Message receivedMsg) {
        // logg the piece as received 
        // process the piece and put it in File Piece Map
        // find the next piece that is needed
        //  broadcast the piece index needed
        stopTime = System.currentTimeMillis();
        PieceMessage pieceMessage = (PieceMessage) receivedMsg;
        System.out.println("Received piece index: " + pieceMessage.getIndex());
        byte[] piece = (byte[]) pieceMessage.findPayLoad();
        assignDataRate(piece.length);

        if (piece.length != 0) {
            this.peerState.insertPieceIntoMap(pieceMessage.getIndex(), piece);
            logger.pieceDownloadCompleted(remotePeerId, pieceMessage.getIndex(), this.peerState.receiveFilePieceIndexMap().size());
            broadcastMessage(new HaveMessage(pieceMessage.getIndex()));
        }
        else {
            System.out.println("Error: piece length is 0!");
        }
        int index = getNextInterestingPieceIndex(BitTorrentState.findPeers().get(remotePeerId).getBitField(),
                this.peerState.getBitField());
        if (index == -1) {
            if (this.peerState.getBitField().nextClearBit(0) == BitTorrentState.pieceCount()) {
                NotInterestedMessage notInterestedMessage = new NotInterestedMessage();
                broadcastMessage(notInterestedMessage);
                System.out.println(this.peerState.getBitField().nextClearBit(0));
                FileUtils.combinePiecesAndOutputFile(this.peerState);
                logger.fileDownloadCompleted();
                if (BitTorrentState.isFileDownloadedbyAll()) {
                    stopAllConnections();
                }
            }
        }
        else {
            System.out.println(this.peerState.getPeerId() + ": Requesting piece Index " + index);
            startTime = System.currentTimeMillis();
            RequestMessage requestMessage = new RequestMessage(index);
            sendMessage(requestMessage);
        }
    }

    private void assignDataRate(int size) {
        // change and set appropriate Data rate
        double dataRate;
        if (Math.abs(stopTime - startTime) > 0) {
            dataRate = size / (stopTime - startTime);
        }
        else {
            dataRate = 0;
        }

        System.out.println("Setting data rate " + dataRate);
        BitTorrentState.findPeers().get(remotePeerId).assignDataRate(dataRate);
    }

    private void processPeerRequest(Message receivedMsg) {
        // receive and process request from the peer.
        RequestMessage requestMessage = (RequestMessage) receivedMsg;
        Integer index = (Integer) requestMessage.findPayLoad();
        String optimisticUnchokedPeerId = this.peerState.getCurrOptUnchPeer();
        if (this.peerState.findPreferPeers().contains(remotePeerId) || (optimisticUnchokedPeerId != null &&
                optimisticUnchokedPeerId.equals(remotePeerId))) {
            if (this.peerState.getBitField().get(index)) {
                PieceMessage pieceMessage = new PieceMessage(this.peerState.receiveFilePieceIndexMap().get(index), index);
                sendMessage(pieceMessage);
            }
            else {
                System.out.println(this.peerState.getPeerId() + ": Error: Discarding request message as piece does not exist!");
            }
        }
        else {
            System.out.println(this.peerState.getPeerId() + ": Discarding request message as peer not in preferred neighbour list!");
        }
    }

    private void processInterestedMessage() {

        // receive interested message from peer and process it.

        logger.receivedInterestedMsg(remotePeerId);

        this.peerState.insertPeersInterested(remotePeerId);

    }

    private void processNotInterestedMessage() {

        //  logg not interested message from peer and process not inerested message
        logger.receivedNotInterestedMsg(remotePeerId);
        this.peerState.discardPeersInterested(remotePeerId);
        if (BitTorrentState.isFileDownloadedbyAll()) {
            stopAllConnections();
        }
    }

    private void processBitField(Message message){
        // Receive BitField Message and process the msg
        BitFieldMessage bitFieldMessage = (BitFieldMessage) message;

        BitTorrentState.findPeers().get(remotePeerId).assignBitfieldValue(bitFieldMessage.findPayLoad());

        int interestingPieceIndex = getNextInterestingPieceIndex(bitFieldMessage.findPayLoad(), this.peerState.getBitField());

        if (interestingPieceIndex == -1) {
            NotInterestedMessage notInterestedMessage = new NotInterestedMessage();
            //sendMessage(notInterestedMessage);
        }
        else {
            InterestedMessage interestedMessage = new InterestedMessage();
            sendMessage(interestedMessage);
            RequestMessage requestMessage = new RequestMessage(interestingPieceIndex);
            startTime = System.currentTimeMillis();
            sendMessage(requestMessage);
        }

    }

    private int getNextInterestingPieceIndex(BitSet remote, BitSet current) {

        // Find the Index of Next Interested Piece
        BitSet interestingPieces = new BitSet();
        interestingPieces.or(remote);
        interestingPieces.andNot(current);
        return interestingPieces.nextSetBit(0);
    }

    private void processHandShakeMessage(Message response) {

        // recieve Handshake message from User and process it.
        HandshakeMessage handshakeMessage = (HandshakeMessage) response;
        this.remotePeerId = handshakeMessage.getPeerId();
        if (BitTorrentState.findPeers().containsKey(remotePeerId)) {
            System.out.println(remotePeerId + " validated!");
        }
        else {
            System.out.println(remotePeerId + " invalid!");
            return;
        }
        if (Integer.parseInt(this.peerState.getPeerId()) < Integer.parseInt(this.remotePeerId)) {
            logger.establishingConnectionFrom(this.remotePeerId);
            this.peerState.conList().put(this.remotePeerId, this);
        }
        BitFieldMessage bitfieldMessage = new BitFieldMessage(this.peerState.getBitField());
        sendMessage(bitfieldMessage);
    }

    public Message receiveMessage() throws IOException, ClassNotFoundException {

        // Process Received Message
        if (is == null) {
            is = new ObjectInputStream(peerSocket.getInputStream());
        }
        return (Message) is.readObject();

    }

    public synchronized void sendMessage(Message message) {
        // Send Message to the peer
        System.out.println(this.peerState.getPeerId() + ": Sending " + message.findMsgType().name() + " message: " + message.toString());
        try {
            os.writeObject(message);
            os.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(Message message) {
        Map<String, PeerConnectionHandler> connections = peerState.conList();

        for (PeerConnectionHandler connection : connections.values()){
            connection.sendMessage(message);
        }
    }

    public void stopAllConnections() {
        for (PeerConnectionHandler peerConnectionHandler: this.peerState.conList().values()) {
            peerConnectionHandler.stop();
        }
    }

    public void stop() {
        try {
            System.out.println(this.peerState.getPeerId() + ": Stopping tasks");
            //asyncMessageSender.interrupt();
            this.peerState.blockAllTasks();
            this.peerState.findServSocket().close();
            running = false;
            os.close();
            is.close();
            System.out.println(this.peerState.getPeerId() + ": Stopped tasks");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
