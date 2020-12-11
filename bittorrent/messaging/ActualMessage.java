package bittorrent.messaging;
/*
    This is The Message Class with size of the message byte array, type of the message
    along with payload that is combination of index of each piece and peice contents.

*/

public abstract class ActualMessage extends Message {

    private int length;
    private Object payload;

    public int findLen() {
        return length;
    }

    public void assignLen(int length) {
        this.length = length;
    }

    public Object findPayLoad() {
        return payload;
    }

    public void assignPayLoad(Object payload) {
        this.payload = payload;
    }

    public String toString(){
        return Integer.toString(this.length) + findMsgType().getValue() + this.payload;
    }
}
