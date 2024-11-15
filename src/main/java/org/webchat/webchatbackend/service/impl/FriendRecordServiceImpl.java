package org.webchat.webchatbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.webchat.webchatbackend.mapper.FriendRecordMapper;
import org.webchat.webchatbackend.mapper.UserRecordMapper;
import org.webchat.webchatbackend.pojo.record.FriendRecord;
import org.webchat.webchatbackend.pojo.record.UserRecord;
import org.webchat.webchatbackend.service.FriendRecordService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendRecordServiceImpl implements FriendRecordService {
    private final FriendRecordMapper friendRecordMapper;
    private final UserRecordMapper userRecordMapper;

    public FriendRecordServiceImpl(FriendRecordMapper friendRecordMapper, UserRecordMapper userRecordMapper) {
        this.friendRecordMapper = friendRecordMapper;
        this.userRecordMapper = userRecordMapper;
    }

    // 保存好友记录
    @Override
    public void saveFriendRecord(FriendRecord friendRecord) {
        // 先判断好友记录是否已经存在，避免重复添加
        QueryWrapper<FriendRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", friendRecord.getUserId())
                .eq("friend_id", friendRecord.getFriendId());

        long count = friendRecordMapper.selectCount(queryWrapper);
        if (count == 0) {
            // 如果没有找到记录，则保存
            friendRecordMapper.insert(friendRecord);
        }
    }

    // 删除好友记录
    @Override
    public void deleteFriendRecord(String userId, String friendId) {
        // 根据 userId 和 friendId 删除对应的记录
        QueryWrapper<FriendRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("friend_id", friendId);
        friendRecordMapper.delete(queryWrapper);
    }

    // 获取指定用户的所有好友的 UserRecord 信息
    @Override
    public List<UserRecord> getFriendRecord(String userId) {
        // 查询所有以 userId 为用户的好友记录
        QueryWrapper<FriendRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        // 获取该用户的所有好友记录
        List<FriendRecord> friendRecords = friendRecordMapper.selectList(queryWrapper);

        // 如果没有好友记录，直接返回空列表
        if (friendRecords.isEmpty()) {
            return Collections.emptyList();
        }

        // 通过 friendId 查找对应的 UserRecord
        List<String> friendIds = friendRecords.stream()
                .map(FriendRecord::getFriendId)
                .collect(Collectors.toList());

        // 使用 friendIds 查询用户表，获取对应的 UserRecord
        QueryWrapper<UserRecord> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("user_id", friendIds);

        // 返回查询到的好友的 UserRecord 列表
        return userRecordMapper.selectList(userQueryWrapper);
    }

}
