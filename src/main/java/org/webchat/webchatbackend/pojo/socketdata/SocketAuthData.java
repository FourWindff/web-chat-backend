package org.webchat.webchatbackend.pojo.socketdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.webchat.webchatbackend.pojo.AuthStatus;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketAuthData implements SocketMessage {
    private Integer statusCode;
    private String message;
    private String username;
    private String userId;
    private String password;
    public SocketAuthData(AuthStatus statusCode, String username, String userId, String password) {
        this.statusCode = statusCode.getCode();
        this.message = statusCode.getMessage();
        this.username = username;
        this.userId = userId;
        this.password = password;
    }


    @Override
    public TextMessage getTextMessage() {
        ObjectMapper objectMapper = new ObjectMapper();
        String type=SocketDataType.LOGIN.getType();
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("type", type);

            dataMap.put("statusCode", statusCode);
            dataMap.put("message", message);

            dataMap.put("username", username);
            dataMap.put("userId", userId);
            dataMap.put("password", password);

            String jsonMessage = objectMapper.writeValueAsString(dataMap);
            return new TextMessage(jsonMessage);  // 返回一个 TextMessage 对象
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return new TextMessage("{}");  // 出现异常时返回一个空 JSON
        }
    }
}
