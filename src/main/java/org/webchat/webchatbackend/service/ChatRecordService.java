package org.webchat.webchatbackend.service;

import org.webchat.webchatbackend.pojo.record.ChatRecord;

import java.util.List;
//获取、设置聊天记录

public interface ChatRecordService {

    List<ChatRecord> getChatRecordBySenderId(String userId);

    List<ChatRecord> getChatRecordByReceiverId(String userId);

    void saveChatRecord(ChatRecord chatRecord);

    void deleteChatRecordBySenderId(String userId);

    void deleteChatRecordByReceiverId(String userId);


}
