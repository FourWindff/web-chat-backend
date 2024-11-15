package org.webchat.webchatbackend.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("friend_record")
public class FriendRecord {
    @TableId
    private Integer id;
    private String userId;
    private String friendId;
}
