package bittorrent.messaging;
/*
    This is The Message Class with size of the message byte array, type of the message

*/
public class HandshakeMessage extends Message {

    private final String header = "P2PFILESHARINGPROJ";
    private final String ZERO_BITS = "0000000000";
    private String peerId;

    //constructor
    public HandshakeMessage(String peerId) {
        this.assignMsgType(MessageType.HANDSHAKE);
        this.peerId = peerId;
    }

    public String toString(){
        return this.header + this.ZERO_BITS + this.peerId;
    }

    public String getPeerId(){
        return peerId;
    }

    //TODO
    public boolean validate(String peerId) {
        return header == "P2PFILESHARINGPROJ" && peerId == this.peerId;
    }
}
