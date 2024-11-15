package org.webchat.webchatbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.webchat.webchatbackend.mapper.UserRecordMapper;
import org.webchat.webchatbackend.pojo.record.UserRecord;
import org.webchat.webchatbackend.service.UserRecordService;

@Service
public class UserRecordServiceImpl implements UserRecordService {

    private final UserRecordMapper userRecordMapper;

    public UserRecordServiceImpl(UserRecordMapper userRecordMapper) {
        this.userRecordMapper = userRecordMapper;
    }

    // 验证用户的密码
    @Override
    public boolean exist(String userId, String password) {
        QueryWrapper<UserRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        // 查询是否存在匹配的用户
        UserRecord userRecord = userRecordMapper.selectOne(queryWrapper);
        return userRecord != null; // 如果找到了匹配的记录，返回 true
    }

    // 保存用户记录
    @Override
    public void saveUserRecord(UserRecord userRecord) {
        // 使用 MyBatis-Plus 插入用户记录
        userRecordMapper.insert(userRecord);
    }

    // 根据 ID 获取用户记录
    @Override
    public UserRecord getUserRecordById(String id) {
        // 使用 MyBatis-Plus 查询
        return userRecordMapper.selectById(id);
    }

    // 根据 ID 删除用户记录
    @Override
    public void deleteUserRecordById(String id) {
        // 使用 MyBatis-Plus 删除用户记录
        userRecordMapper.deleteById(id);
    }
}
