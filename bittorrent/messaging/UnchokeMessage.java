package bittorrent.messaging;
/*
    This is The Message Class with size of the message byte array, type of the message
    along with payload that is combination of index of each piece and peice contents.

*/
public class UnchokeMessage extends ActualMessage {

    //constructor
    public UnchokeMessage(){
        super.assignMsgType(MessageType.UNCHOKE);
        super.assignLen(1);
        super.assignPayLoad("");
    }

}
