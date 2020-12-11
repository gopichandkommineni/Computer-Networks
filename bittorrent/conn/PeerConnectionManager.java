package bittorrent.conn;

import bittorrent.state.BitTorrentStatus;
import bittorrent.state.PeerStatus;
import bittorrent.messaging.*;
import bittorrent.ops.FileOps;
import bittorrent.ops.Logger;

import java.io.*;
import java.net.Socket;
import java.util.BitSet;
import java.util.Map;

public class PeerConnectionManager implements Runnable{

    private Socket peerSocket = null;
    private PeerStatus peerState = null;
    private ObjectInputStream is;
    private ObjectOutputStream os;
    private Logger logger;
    private String remotePeerId;
    private long startTime;
    private long stopTime;
    private boolean running = false;
    private Thread asyncMessageSender;

    //constructor
    public PeerConnectionManager(Socket peerSocket, PeerStatus peerState) {
        this.peerSocket = peerSocket;
        this.peerState = peerState;
        this.logger = Logger.fetchLogger(peerState.getPeerId());
    }

    public void assignRemotePeerId(String remotePeerId) {

        // allotting peer, a remote peer Id
        this.remotePeerId = remotePeerId;
    }

    @Override
    public void run() {
        try
        {
            running = true;
            os = new ObjectOutputStream(peerSocket.getOutputStream());

            // sending a new handshake msg to the new peer connection
            sendMessage(new HandshakeMessage(this.peerState.getPeerId()));

            Message receivedMsg = null;
            while (running) {
                receivedMsg = receiveMessage();

                // obtained message from the peer
                System.out.println(this.peerState.getPeerId() + ": Received message type: " +
                        receivedMsg.findMsgType().name() + " from " + this.remotePeerId + ", message: " +
                        receivedMsg.toString());

                // determine the type of message
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
            // exit in case of exception
            System.out.println(this.peerState.getPeerId() + ": Exiting PeerConnectionManager because of " + ex.getStackTrace()[0]);
            stop();
        }
    }

    private void processChokeMessage() {

        // Logging choked message
        logger.choked(remotePeerId);
    }

    private void processUnChokeMessage() {

        // output the message to log file and continue with the next interested piece
        logger.unchoked(remotePeerId);

        int interestingPieceIndex = getNextInterestingPieceIndex(BitTorrentStatus.findPeers().get(remotePeerId).getBitField(), this.peerState.getBitField());

        if (interestingPieceIndex != -1) {
            RequestMessage requestMessage = new RequestMessage(interestingPieceIndex);
            sendMessage(requestMessage);
        }
    }

    private void processHaveMessage(Message receivedMsg) {

        // forward interested message in case peer has the piece
        HaveMessage haveMessage = (HaveMessage) receivedMsg;
        int index = (int) haveMessage.findPayLoad();

        logger.receivedHaveMsg(remotePeerId, index);

        // setting the peer bitset information
        BitTorrentStatus.findPeers().get(remotePeerId).getBitField().set(index);

        if (!this.peerState.getBitField().get(index)) {
            System.out.println(this.peerState.getPeerId() + "Sending interested message for piece" + index);
            sendMessage(new InterestedMessage());
            RequestMessage requestMessage = new RequestMessage(index);
            sendMessage(requestMessage);
        }
    }

    private void processPiece(Message receivedMsg) {

        // log the piece when received
        // set it in File Piece map after processing the piece
        // deterine the next piece that is required
        //  broadcast the required piece index

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
        int index = getNextInterestingPieceIndex(BitTorrentStatus.findPeers().get(remotePeerId).getBitField(),
                this.peerState.getBitField());
        if (index == -1) {
            if (this.peerState.getBitField().nextClearBit(0) == BitTorrentStatus.pieceCount()) {
                NotInterestedMessage notInterestedMessage = new NotInterestedMessage();
                broadcastMessage(notInterestedMessage);
                System.out.println(this.peerState.getBitField().nextClearBit(0));
                FileOps.combinePiecesAndOutputFile(this.peerState);
                logger.fileDownloadCompleted();
                if (BitTorrentStatus.isFileDownloadedbyAll()) {
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

        // alter and set suitable data rate
        double dataRate;
        if (Math.abs(stopTime - startTime) > 0) {
            dataRate = size / (stopTime - startTime);
        }
        else {
            dataRate = 0;
        }

        System.out.println("Setting data rate " + dataRate);
        BitTorrentStatus.findPeers().get(remotePeerId).assignDataRate(dataRate);
    }

    private void processPeerRequest(Message receivedMsg) {

        // gather and process the request message from peer.
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

        // get interested message from the peer and process the message

        logger.receivedInterestedMsg(remotePeerId);

        this.peerState.insertPeersInterested(remotePeerId);

    }

    private void processNotInterestedMessage() {

        //  log and process the not interested message from peer
        logger.receivedNotInterestedMsg(remotePeerId);
        this.peerState.discardPeersInterested(remotePeerId);
        if (BitTorrentStatus.isFileDownloadedbyAll()) {
            stopAllConnections();
        }
    }

    private void processBitField(Message message){

        // get the BitField Message and process it
        BitFieldMessage bitFieldMessage = (BitFieldMessage) message;

        BitTorrentStatus.findPeers().get(remotePeerId).assignBitfieldValue(bitFieldMessage.findPayLoad());

        int interestingPieceIndex = getNextInterestingPieceIndex(bitFieldMessage.findPayLoad(), this.peerState.getBitField());

        if (interestingPieceIndex == -1) {
            NotInterestedMessage notInterestedMessage = new NotInterestedMessage();

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

        // obtain the next interested piece Index
        BitSet interestingPieces = new BitSet();
        interestingPieces.or(remote);
        interestingPieces.andNot(current);
        return interestingPieces.nextSetBit(0);
    }

    private void processHandShakeMessage(Message response) {

        // get the handshake message from User and process the message
        HandshakeMessage handshakeMessage = (HandshakeMessage) response;
        this.remotePeerId = handshakeMessage.getPeerId();
        if (BitTorrentStatus.findPeers().containsKey(remotePeerId)) {
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

        // Process the acquired message
        if (is == null) {
            is = new ObjectInputStream(peerSocket.getInputStream());
        }
        return (Message) is.readObject();

    }

    public synchronized void sendMessage(Message message) {

        // forward the message to peer
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
        Map<String, PeerConnectionManager> connections = peerState.conList();

        for (PeerConnectionManager connection : connections.values()){
            connection.sendMessage(message);
        }
    }

    public void stopAllConnections() {
        for (PeerConnectionManager peerConnectionHandler: this.peerState.conList().values()) {
            peerConnectionHandler.stop();
        }
    }

    public void stop() {
        try {
            System.out.println(this.peerState.getPeerId() + ": Stopping tasks");

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
