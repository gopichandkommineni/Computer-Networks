package bittorrent.messaging;

public class UnchokeMessage extends ActualMessage {

    public UnchokeMessage(){
        super.assignMsgType(MessageType.UNCHOKE);
        super.assignLen(1);
        super.assignPayLoad("");
    }

}
