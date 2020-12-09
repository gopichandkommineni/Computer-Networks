package com.bittorrent.messaging;

import java.util.BitSet;

public class BitFieldMessage extends ActualMessage {

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
