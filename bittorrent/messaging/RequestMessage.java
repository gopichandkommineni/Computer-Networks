package bittorrent.messaging;
/*
    This is The Message Class with size of the message byte array, type of the message
    along with payload that is combination of index of each piece and peice contents.

*/
public class RequestMessage extends ActualMessage{

    //constructor
    public RequestMessage(Integer index) {
        super.assignMsgType(MessageType.REQUEST);
        super.assignLen(5);
        super.assignPayLoad(index);
    }
}
