package org.webchat.webchatbackend.pojo.socketdata;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SocketDataType {
    LOGIN("login","登录"),
    REGISTER("register","注册"),
    TEXT_CHAT("textChat","转发聊天内容"),
    FILE_CHAT("fileChat","转发文件名"),
    LINK("link","转发SDP、ICE"),
    FILE("file","转发文件"),
    INIT_FRIEND_LIST("initFriendList","获取好友列表"),
    INIT_OFFLINE_CHAT_LIST("initOfflineChatList","获取好友离线时未接收的消息"),
    ADD_FRIEND_OFFER("addFriendOffer","添加好友请求"),
    ADD_FRIEND_ANSWER("addFriendAnswer","添加好友回复"),
    ADD_FRIEND_SUCCESS("addFriendSuccess","同意添加好友"),
    ADD_FRIEND_FAIL("addFriendFail","拒绝添加好友"),
    DELETE_FRIEND("deleteFriend","删除好友");


    private final String type;
    private final String message;

    SocketDataType(String type, String message) {
        this.type = type;
        this.message = message;
    }

    @JsonValue
    public String getType() {
        return type;
    }

    @JsonValue
    public String getMessage() {
        return message;
    }
    public static SocketDataType fromString(String type) {
        for (SocketDataType socketDataType : SocketDataType.values()) {
            if (socketDataType.type.equals(type)) {
                return socketDataType;
            }
        }
        return null;
    }
}
