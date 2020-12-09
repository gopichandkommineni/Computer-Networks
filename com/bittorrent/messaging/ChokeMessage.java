package com.bittorrent.messaging;

public class ChokeMessage extends ActualMessage {

    public ChokeMessage(){
        super.assignMsgType(MessageType.CHOKE);
        super.assignLen(1);
        super.assignPayLoad("");
    }

}
