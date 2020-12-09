package bittorrent.messaging;

import java.io.Serializable;

public abstract class Message implements Serializable {

    public static final long serialVersionUID = 1L;

    private MessageType messageType = null;

    public MessageType findMsgType() {
        return messageType;
    }

    public void assignMsgType(MessageType messageType) {
        this.messageType = messageType;
    }

    public abstract String toString();
}
