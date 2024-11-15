package org.webchat.webchatbackend.pojo.socketdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.webchat.webchatbackend.pojo.record.ChatRecord;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SocketChatListData implements SocketMessage {
    private List<ChatRecord> chatRecordList;

    @Override
    public TextMessage getTextMessage() {
        ObjectMapper objectMapper = new ObjectMapper();
        String type = SocketDataType.INIT_OFFLINE_CHAT_LIST.getType();
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("type", type);
            dataMap.put("chatRecordList", chatRecordList);
            String jsonMessage = objectMapper.writeValueAsString(dataMap);
            return new TextMessage(jsonMessage);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return new TextMessage("{}");
        }

    }
}
