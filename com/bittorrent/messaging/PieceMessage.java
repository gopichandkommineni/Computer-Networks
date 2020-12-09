package com.bittorrent.messaging;

public class PieceMessage extends ActualMessage{

    private int index;

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
