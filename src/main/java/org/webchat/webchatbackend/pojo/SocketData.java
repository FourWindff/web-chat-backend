package org.webchat.webchatbackend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//用来接收前端发送来的消息，通用的、混合的
//某些字段可能为空，根据type和前端发的内容的自行判断
public class SocketData {
    private String type;
    private String sourceUserId;
    private String targetUserId;
    private String username;
    private String password;
    private Long createAt;
    private String content;
    private Integer size;
}
