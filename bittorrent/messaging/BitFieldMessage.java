package bittorrent.messaging;

import java.util.BitSet;
/*
    This is The Message Class with size of the message byte array, type of the message
    along with payload that is combination of index of each piece and peice contents.

*/
public class BitFieldMessage extends ActualMessage {

    //constructor
    public BitFieldMessage(BitSet bitField) {
        super.assignMsgType(MessageType.BITFIELD);
        Object payload = bitField;
        super.assignLen(1 + bitField.size());
        super.assignPayLoad(payload);
    }

    public BitSet findPayLoad() {
        return (BitSet) super.findPayLoad();
    }
}
