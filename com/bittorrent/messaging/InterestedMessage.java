package com.bittorrent.messaging;

public class InterestedMessage extends ActualMessage {

    public InterestedMessage(){
        super.setMessageType(MessageType.INTERESTED);
        super.setLength(1);
        super.setPayload("");
    }

}
