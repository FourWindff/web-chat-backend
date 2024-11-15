package org.webchat.webchatbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.webchat.webchatbackend.pojo.record.ChatRecord;
import org.webchat.webchatbackend.mapper.ChatRecordMapper;
import org.webchat.webchatbackend.service.ChatRecordService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatRecordServiceImpl implements ChatRecordService {

    private final ChatRecordMapper chatRecordMapper;  // 注入 MyBatis-Plus Mapper


    public ChatRecordServiceImpl(ChatRecordMapper chatRecordMapper) {
        this.chatRecordMapper = chatRecordMapper;
    }


    @Override
    public List<ChatRecord> getChatRecordBySenderId(String userId) {
        return chatRecordMapper.selectList(new QueryWrapper<ChatRecord>().eq("sender_id", userId));
    }

    @Override
    public List<ChatRecord> getChatRecordByReceiverId(String userId) {
        return chatRecordMapper.selectList(new QueryWrapper<ChatRecord>().eq("receiver_id", userId));
    }

    @Override
    public void saveChatRecord(ChatRecord chatRecord) {
        chatRecordMapper.insert(chatRecord);
    }

    @Override
    public void deleteChatRecordBySenderId(String userId) {
        chatRecordMapper.delete(new QueryWrapper<ChatRecord>().eq("sender_id", userId));
    }

    @Override
    public void deleteChatRecordByReceiverId(String userId) {
        chatRecordMapper.delete(new QueryWrapper<ChatRecord>().eq("receiver_id", userId));
    }
}
