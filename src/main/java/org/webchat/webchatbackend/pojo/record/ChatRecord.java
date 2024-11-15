package org.webchat.webchatbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("chat_record")
public class ChatRecord {
    @TableId
    private Integer id;
    private String senderId;
    private String receiverId;
    private String messageType;
    private String messageContent;
    private long createAt;

    public ChatRecord(String senderId, String receiverId, String messageType, String messageContent, long createAt) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageType = messageType;
        this.messageContent = messageContent;
        this.createAt = createAt;
    }
    public ChatRecord() {}
}
