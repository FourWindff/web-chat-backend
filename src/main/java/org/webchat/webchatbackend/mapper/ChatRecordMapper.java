package org.webchat.webchatbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.webchat.webchatbackend.pojo.ChatRecord;
@Mapper
public interface ChatRecordMapper extends BaseMapper<ChatRecord> {
}
