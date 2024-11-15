package org.webchat.webchatbackend.pojo.record;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("user_record")
@AllArgsConstructor
@NoArgsConstructor
public class UserRecord {
    @TableId
    private String userId;
    private String username;
    @JsonIgnore
    private String password;

}
