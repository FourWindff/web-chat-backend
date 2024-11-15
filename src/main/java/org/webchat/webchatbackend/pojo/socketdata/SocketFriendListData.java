package org.webchat.webchatbackend.pojo.socketdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.webchat.webchatbackend.pojo.record.UserRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketFriendListData {
    private String userId;
    private List<UserRecord> friendList;

    public TextMessage getTextMessage() {
        ObjectMapper objectMapper = new ObjectMapper();
        String type=SocketDataType.INIT_FRIEND_LIST.getType();
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("type", type);
            dataMap.put("userId", userId);
            dataMap.put("friendList", friendList);
            String jsonMessage = objectMapper.writeValueAsString(dataMap);
            return new TextMessage(jsonMessage);  // 返回一个 TextMessage 对象
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return new TextMessage("{}");  // 出现异常时返回一个空 JSON
        }
    }
}
