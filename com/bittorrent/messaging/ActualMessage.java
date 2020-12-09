package com.bittorrent.messaging;

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
