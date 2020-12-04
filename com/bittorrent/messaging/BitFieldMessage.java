package com.bittorrent.messaging;

import java.util.BitSet;

public class BitFieldMessage extends ActualMessage {

    public BitFieldMessage(BitSet bitField) {
        super.setMessageType(MessageType.BITFIELD);
        Object payload = bitField;
        super.setLength(1 + bitField.size());
        super.setPayload(payload);
    }

    public BitSet getPayload() {
        return (BitSet) super.getPayload();
    }
}
