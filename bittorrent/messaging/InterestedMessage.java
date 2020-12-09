package bittorrent.messaging;

public class InterestedMessage extends ActualMessage {

    public InterestedMessage(){
        super.assignMsgType(MessageType.INTERESTED);
        super.assignLen(1);
        super.assignPayLoad("");
    }

}
