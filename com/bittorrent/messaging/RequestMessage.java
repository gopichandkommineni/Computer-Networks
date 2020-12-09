package com.bittorrent.messaging;

public class RequestMessage extends ActualMessage{

    public RequestMessage(Integer index) {
        super.assignMsgType(MessageType.REQUEST);
        super.assignLen(5);
        super.assignPayLoad(index);
    }
}
