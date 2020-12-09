package com.bittorrent.messaging;

public class NotInterestedMessage extends ActualMessage {

    public NotInterestedMessage(){
        super.assignMsgType(MessageType.NOT_INTERESTED);
        super.assignLen(1);
        super.assignPayLoad("");
    }

}
