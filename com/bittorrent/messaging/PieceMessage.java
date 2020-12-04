package com.bittorrent.messaging;

public class PieceMessage extends ActualMessage{

    private int index;

    public PieceMessage(byte[] payload, int index) {
        this.index = index;
        super.setMessageType(MessageType.PIECE);
        super.setLength(5 + payload.length);
        super.setPayload(payload);
    }

    public int getIndex() {
        return this.index;
    }

}
