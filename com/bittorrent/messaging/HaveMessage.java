package com.bittorrent.messaging;

public class HaveMessage extends ActualMessage{

    public HaveMessage(Integer index) {
        super.setMessageType(MessageType.HAVE);
        super.setLength(5);
        super.setPayload(index);
    }
}
