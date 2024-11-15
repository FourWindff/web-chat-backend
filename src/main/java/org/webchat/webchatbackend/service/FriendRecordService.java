package org.webchat.webchatbackend.service;

import org.webchat.webchatbackend.pojo.record.FriendRecord;
import org.webchat.webchatbackend.pojo.record.UserRecord;

import java.util.List;

//增加、获取、删除朋友记录
public interface FriendRecordService {
    void saveFriendRecord(FriendRecord friendRecord);

    void deleteFriendRecord(String userId,String friendId);

    List<UserRecord> getFriendRecord(String userId);
}
