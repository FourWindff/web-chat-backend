package org.webchat.webchatbackend.pojo;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketTextData {
    private String type;
    private String sourceUserId;
    private String targetUserId;
    private String username;
    private String password;
    private Long createAt;
    private String content;
}
