package com.bittorrent.messaging;

public class RequestMessage extends ActualMessage{

    public RequestMessage(Integer index) {
        super.setMessageType(MessageType.REQUEST);
        super.setLength(5);
        super.setPayload(index);
    }
}
