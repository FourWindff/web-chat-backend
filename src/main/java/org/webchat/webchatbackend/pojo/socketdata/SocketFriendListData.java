package org.webchat.webchatbackend.pojo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.socket.TextMessage;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketFriendListData {
    private String userId;
    private List<UserRecord> friendList;

    public TextMessage parseSocketMessage() {
        // 使用 ObjectMapper 转换为 JSON 字符串
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // 将当前对象序列化为 JSON
            String jsonMessage = objectMapper.writeValueAsString(this);
            return new TextMessage(jsonMessage);  // 返回一个 TextMessage 对象
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
