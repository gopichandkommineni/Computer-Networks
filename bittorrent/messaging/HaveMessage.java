package bittorrent.messaging;

public class HaveMessage extends ActualMessage{

    public HaveMessage(Integer index) {
        super.assignMsgType(MessageType.HAVE);
        super.assignLen(5);
        super.assignPayLoad(index);
    }
}
