package org.webchat.webchatbackend.pojo.socketdata;

import org.springframework.web.socket.TextMessage;

public interface SocketMessage {
    TextMessage getTextMessage();
}
