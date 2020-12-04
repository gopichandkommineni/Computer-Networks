package com.bittorrent.messaging;

public class UnchokeMessage extends ActualMessage {

    public UnchokeMessage(){
        super.setMessageType(MessageType.UNCHOKE);
        super.setLength(1);
        super.setPayload("");
    }

}
