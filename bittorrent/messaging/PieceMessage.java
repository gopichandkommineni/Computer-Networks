package bittorrent.messaging;
/*
    This is The Message Class with size of the message byte array, type of the message
    along with payload that is combination of index of each piece and peice contents.

*/
public class PieceMessage extends ActualMessage{

    private int index;

    //constructor
    public PieceMessage(byte[] payload, int index) {
        this.index = index;
        super.assignMsgType(MessageType.PIECE);
        super.assignLen(5 + payload.length);
        super.assignPayLoad(payload);
    }

    public int getIndex() {
        return this.index;
    }

}
