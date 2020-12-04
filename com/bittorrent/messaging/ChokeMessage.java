package com.bittorrent.messaging;

public class ChokeMessage extends ActualMessage {

    public ChokeMessage(){
        super.setMessageType(MessageType.CHOKE);
        super.setLength(1);
        super.setPayload("");
    }

}
