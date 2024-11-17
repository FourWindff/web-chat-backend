package org.webchat.webchatbackend.pojo.record;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("friend_record")
public class FriendRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String userId;
    private String friendId;
    public FriendRecord(String sourceUserId, String targetUserId) {
        this.userId = sourceUserId;
        this.friendId = targetUserId;
    }
}
