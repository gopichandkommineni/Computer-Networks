package com.bittorrent.messaging;

public class NotInterestedMessage extends ActualMessage {

    public NotInterestedMessage(){
        super.setMessageType(MessageType.NOT_INTERESTED);
        super.setLength(1);
        super.setPayload("");
    }

}
