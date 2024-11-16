package org.webchat.webchatbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.Transactional;
import org.webchat.webchatbackend.pojo.record.ChatRecord;
import org.webchat.webchatbackend.mapper.ChatRecordMapper;
import org.webchat.webchatbackend.pojo.socketdata.SocketDataType;
import org.webchat.webchatbackend.service.ChatRecordService;
import org.springframework.stereotype.Service;

import java.util.List;
@Transactional
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
    public List<ChatRecord> getFileRecordBySenderId(String userId) {
        QueryWrapper<ChatRecord> queryWrapper = new QueryWrapper<ChatRecord>().eq("sender_id", userId).eq("message_type", SocketDataType.FILE_CHAT.getType());
        return chatRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<ChatRecord> getFileRecordByReceiverId(String userId) {
        QueryWrapper<ChatRecord> queryWrapper = new QueryWrapper<ChatRecord>().eq("receiver_id", userId).eq("message_type", SocketDataType.FILE_CHAT.getType());
        return chatRecordMapper.selectList(queryWrapper);
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
    public void deleteChatRecordById(Integer id) {
        chatRecordMapper.deleteById(id);
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
