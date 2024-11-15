package org.webchat.webchatbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SocketAuthData {
    private Integer statusCode;
    private String message;
    private String username;
    private String userId;

    public SocketAuthData(AuthStatus authStatus, String username, String userId) {
        this.statusCode = authStatus.getCode();
        this.message = authStatus.getMessage();
        this.username = username;
        this.userId = userId;
    }
    public SocketAuthData(){};

}
