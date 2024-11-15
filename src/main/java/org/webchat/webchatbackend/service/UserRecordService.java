package org.webchat.webchatbackend.service;


import org.webchat.webchatbackend.pojo.record.UserRecord;

//设置、获取、删除用户记录
public interface UserRecordService {
    boolean exist(String userId, String password);
    void saveUserRecord(UserRecord userRecord);
    UserRecord getUserRecordById(String id);
    void deleteUserRecordById(String id);
}
