package com.bittorrent.messaging;

import java.io.Serializable;

public abstract class Message implements Serializable {

    public static final long serialVersionUID = 1L;

    private MessageType messageType = null;

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public abstract String toString();
}
