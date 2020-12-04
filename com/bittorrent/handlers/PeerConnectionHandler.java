package com.bittorrent.handlers;

import com.bittorrent.dtos.BitTorrentState;
import com.bittorrent.dtos.PeerState;
import com.bittorrent.messaging.*;
import com.bittorrent.utils.FileUtils;
import com.bittorrent.utils.Logger;

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
        this.logger = Logger.getLogger(peerState.getPeerId());
    }

    public void setRemotePeerId(String remotePeerId) {
        this.remotePeerId = remotePeerId;
    }

    @Override
    public void run() {
        try
        {
            running = true;
            os = new ObjectOutputStream(peerSocket.getOutputStream());

            sendMessage(new HandshakeMessage(this.peerState.getPeerId()));

            Message receivedMsg = null;
            while (running) {
                receivedMsg = receiveMessage();
                System.out.println(this.peerState.getPeerId() + ": Received message type: " +
                        receivedMsg.getMessageType().name() + " from " + this.remotePeerId + ", message: " +
                        receivedMsg.toString());

                switch (receivedMsg.getMessageType()) {
                    case HANDSHAKE: {
                        processHandshake(receivedMsg);
                        break;
                    }
                    case BITFIELD: {
                        processBitField(receivedMsg);
                        break;
                    }
                    case INTERESTED: {
                        processInterested();
                        break;
                    }
                    case NOT_INTERESTED: {
                        processNotInterested();
                        break;
                    }
                    case REQUEST: {
                        processRequest(receivedMsg);
                        break;
                    }
                    case PIECE: {
                        processPiece(receivedMsg);
                        break;
                    }
                    case HAVE: {
                        processHave(receivedMsg);
                        break;
                    }
                    case CHOKE: {
                        processChoke();
                        break;
                    }
                    case UNCHOKE: {
                        processUnchoke();
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

    private void processChoke() {
        logger.logChokingEvent(remotePeerId);
    }

    private void processUnchoke() {

        logger.logUnchokingEvent(remotePeerId);

        int interestingPieceIndex = getNextInterestingPieceIndex(BitTorrentState.getPeers().get(remotePeerId).getBitField(), this.peerState.getBitField());

        if (interestingPieceIndex != -1) {
            RequestMessage requestMessage = new RequestMessage(interestingPieceIndex);
            sendMessage(requestMessage);
        }
    }

    private void processHave(Message receivedMsg) {
        HaveMessage haveMessage = (HaveMessage) receivedMsg;
        int index = (int) haveMessage.getPayload();

        logger.logReceivedHaveMessage(remotePeerId, index);

        // set peer bitset info
        BitTorrentState.getPeers().get(remotePeerId).getBitField().set(index);

        if (!this.peerState.getBitField().get(index)) {
            System.out.println(this.peerState.getPeerId() + "Sending interested message for piece" + index);
            sendMessage(new InterestedMessage());
            RequestMessage requestMessage = new RequestMessage(index);
            sendMessage(requestMessage);
        }
    }

    private void processPiece(Message receivedMsg) {
        stopTime = System.currentTimeMillis();
        PieceMessage pieceMessage = (PieceMessage) receivedMsg;
        System.out.println("Received piece index: " + pieceMessage.getIndex());
        byte[] piece = (byte[]) pieceMessage.getPayload();
        assignDataRate(piece.length);

        if (piece.length != 0) {
            this.peerState.insertPieceIntoMap(pieceMessage.getIndex(), piece);
            logger.logPieceDownloadComplete(remotePeerId, pieceMessage.getIndex(), this.peerState.receiveFilePieceIndexMap().size());
            broadcastMessage(new HaveMessage(pieceMessage.getIndex()));
        }
        else {
            System.out.println("Error: piece length is 0!");
        }
        int index = getNextInterestingPieceIndex(BitTorrentState.getPeers().get(remotePeerId).getBitField(),
                this.peerState.getBitField());
        if (index == -1) {
            if (this.peerState.getBitField().nextClearBit(0) == BitTorrentState.getNumberOfPieces()) {
                NotInterestedMessage notInterestedMessage = new NotInterestedMessage();
                broadcastMessage(notInterestedMessage);
                System.out.println(this.peerState.getBitField().nextClearBit(0));
                FileUtils.joinPiecesAndWriteFile(this.peerState);
                logger.logDownloadComplete();
                if (BitTorrentState.hasAllPeersDownloadedFile()) {
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
        double dataRate;
        if (Math.abs(stopTime - startTime) > 0) {
            dataRate = size / (stopTime - startTime);
        }
        else {
            dataRate = 0;
        }

        System.out.println("Setting data rate " + dataRate);
        BitTorrentState.getPeers().get(remotePeerId).assignDataRate(dataRate);
    }

    private void processRequest(Message receivedMsg) {
        RequestMessage requestMessage = (RequestMessage) receivedMsg;
        Integer index = (Integer) requestMessage.getPayload();
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

    private void processInterested() {

        logger.logInterestedMessageReceived(remotePeerId);

        this.peerState.insertPeersInterested(remotePeerId);

    }

    private void processNotInterested() {
        logger.logNotInterestedMessageReceived(remotePeerId);
        this.peerState.discardPeersInterested(remotePeerId);
        if (BitTorrentState.hasAllPeersDownloadedFile()) {
            stopAllConnections();
        }
    }

    private void processBitField(Message message){
        BitFieldMessage bitFieldMessage = (BitFieldMessage) message;

        BitTorrentState.getPeers().get(remotePeerId).assignBitfieldValue(bitFieldMessage.getPayload());

        int interestingPieceIndex = getNextInterestingPieceIndex(bitFieldMessage.getPayload(), this.peerState.getBitField());

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
        BitSet interestingPieces = new BitSet();
        interestingPieces.or(remote);
        interestingPieces.andNot(current);
        return interestingPieces.nextSetBit(0);
    }

    private void processHandshake(Message response) {
        HandshakeMessage handshakeMessage = (HandshakeMessage) response;
        this.remotePeerId = handshakeMessage.getPeerId();
        if (BitTorrentState.getPeers().containsKey(remotePeerId)) {
            System.out.println(remotePeerId + " validated!");
        }
        else {
            System.out.println(remotePeerId + " invalid!");
            return;
        }
        if (Integer.parseInt(this.peerState.getPeerId()) < Integer.parseInt(this.remotePeerId)) {
            logger.logTcpConnectionFrom(this.remotePeerId);
            this.peerState.conList().put(this.remotePeerId, this);
        }
        BitFieldMessage bitfieldMessage = new BitFieldMessage(this.peerState.getBitField());
        sendMessage(bitfieldMessage);
    }

    public Message receiveMessage() throws IOException, ClassNotFoundException {
        if (is == null) {
            is = new ObjectInputStream(peerSocket.getInputStream());
        }
        return (Message) is.readObject();

    }

    public synchronized void sendMessage(Message message) {
        System.out.println(this.peerState.getPeerId() + ": Sending " + message.getMessageType().name() + " message: " + message.toString());
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
