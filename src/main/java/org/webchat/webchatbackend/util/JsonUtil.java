package org.webchat.webchatbackend.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.webchat.webchatbackend.pojo.SocketData;

@Slf4j
@Component
public class JsonUtil {

    public SocketData fromJsonForText(String json) {
        // 使用 Jackson 的 ObjectMapper 将 JSON 字符串转换为 JsonNode
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("json反序列化异常");
        }
        String type = rootNode.get("type").asText();
        String sourceUserId = rootNode.get("sourceUserId").asText();
        String targetUserId = rootNode.path("targetUserId").isMissingNode() ? null : rootNode.get("targetUserId").asText();
        String username = rootNode.path("username").isMissingNode() ? null : rootNode.get("username").asText();
        String password = rootNode.path("password").isMissingNode() ? null : rootNode.get("password").asText();
        Long createAt = rootNode.path("createAt").isMissingNode() ? null : rootNode.get("createAt").asLong();
        String content = rootNode.path("content").isMissingNode() ? null : rootNode.get("content").asText();
        Integer size = rootNode.path("size").isMissingNode() ? null : rootNode.get("size").asInt();

        return new SocketData(type, sourceUserId, targetUserId, username, password, createAt, content, size);

    }

    public String parseJsonForText(SocketData socketData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(socketData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("对象序列化失败");
        }
    }

}
