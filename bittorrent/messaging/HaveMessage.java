package bittorrent.messaging;
/*
    This is The Message Class with size of the message byte array, type of the message
    along with payload that is combination of index of each piece and peice contents.

*/
public class HaveMessage extends ActualMessage{

    //constructor
    public HaveMessage(Integer index) {
        super.assignMsgType(MessageType.HAVE);
        super.assignLen(5);
        super.assignPayLoad(index);
    }
}
