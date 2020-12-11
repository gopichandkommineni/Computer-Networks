package bittorrent.messaging;
/*
    This is The Message Class with size of the message byte array, type of the message
    along with payload that is combination of index of each piece and peice contents.

*/
public class InterestedMessage extends ActualMessage {

    //constructor
    public InterestedMessage(){
        super.assignMsgType(MessageType.INTERESTED);
        super.assignLen(1);
        super.assignPayLoad("");
    }

}
