package org.webchat.webchatbackend.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.webchat.webchatbackend.pojo.*;
import org.webchat.webchatbackend.pojo.record.ChatRecord;
import org.webchat.webchatbackend.pojo.record.UserRecord;
import org.webchat.webchatbackend.pojo.socketdata.SocketAuthData;
import org.webchat.webchatbackend.pojo.socketdata.SocketChatListData;
import org.webchat.webchatbackend.pojo.socketdata.SocketDataType;
import org.webchat.webchatbackend.pojo.socketdata.SocketFriendListData;
import org.webchat.webchatbackend.service.ChatRecordService;
import org.webchat.webchatbackend.service.FriendRecordService;
import org.webchat.webchatbackend.service.UserRecordService;
import org.webchat.webchatbackend.util.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private static final ConcurrentHashMap<String, WebSocketSession> sessionsList = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> clientIdList = new ConcurrentHashMap<>();


    private final JsonUtil jsonUtil;
    private final ChatRecordService chatRecordService;
    private final UserRecordService userRecordService;
    private final FriendRecordService friendRecordService;

    public ChatWebSocketHandler(JsonUtil jsonUtil, ChatRecordService chatRecordService, UserRecordService userRecordService, FriendRecordService friendRecordService) {
        this.jsonUtil = jsonUtil;
        this.chatRecordService = chatRecordService;
        this.userRecordService = userRecordService;
        this.friendRecordService = friendRecordService;
    }

    // 处理 WebSocket 连接打开时的行为
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket成功与：{}建立连接 ", session.getId());
    }

    // 处理接收到的消息 type="login" “registry” "chat" "link"
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String sessionId = session.getId();
        String payload = message.getPayload();
        log.info("接收到数据包：{}", payload);
        SocketData socketData = jsonUtil.fromJsonForText(payload);
        //解析出数据包中的元数据，data字段除外
        SocketDataType type = SocketDataType.fromString(socketData.getType());
        if (type == null) return;

        String sourceUserId = socketData.getSourceUserId();
        //可选
        String targetUserId = socketData.getTargetUserId();
        String username = socketData.getUsername();
        String password = socketData.getPassword();


        switch (type) {
            //用户登录操作，返回登录情况，返回用户名，用户id
            //需要 sourceUserId，password
            case LOGIN:
                if (userRecordService.exist(sourceUserId, password)) {
                    UserRecord currentUser = userRecordService.getUserRecordById(sourceUserId);

                    TextMessage result = new SocketAuthData(AuthStatus.LOGIN_SUCCESS, currentUser.getUsername(), currentUser.getUserId(), password).getTextMessage();
                    log.info(result.toString());
                    //发送登录状态
                    session.sendMessage(result);
                    //加入登录状态map
                    if (!sessionsList.containsKey(sourceUserId)) {
                        sessionsList.put(sourceUserId, session);
                        clientIdList.put(sessionId, sourceUserId);
                    }
                    log.info("用户:{}登录成功，会话id：{}", sourceUserId, sessionId);
                } else {
                    TextMessage result = new SocketAuthData(AuthStatus.LOGIN_FAIL_INVALID_CREDENTIALS, null, null, null).getTextMessage();
                    session.sendMessage(result);
                    log.info("用户:{}登录失败，会话id：{}", sourceUserId, sessionId);
                }
                break;


            //用户注册操作，返回注册情况
            //需要 sourceUserId，username，password
            case REGISTRY:
                if (userRecordService.exist(sourceUserId, password)) {
                    TextMessage result = new SocketAuthData(AuthStatus.REGISTER_FAIL_USERNAME_EXISTS, null, null, null).getTextMessage();
                    session.sendMessage(result);
                    log.info("用户:{}注册失败，会话id：{}", sourceUserId, sessionId);
                } else {
                    TextMessage result = new SocketAuthData(AuthStatus.REGISTER_SUCCESS, username, sourceUserId, password).getTextMessage();
                    session.sendMessage(result);
                    if (username.isEmpty() || sourceUserId.isEmpty() || password.isEmpty()) return;
                    //添加用户记录
                    UserRecord newUser = new UserRecord(sourceUserId, username, password);
                    userRecordService.saveUserRecord(newUser);
                    //添加用户会话状态
                    if (!sessionsList.containsKey(sourceUserId)) {
                        sessionsList.put(sourceUserId, session);
                        clientIdList.put(sessionId, sourceUserId);
                    }
                    log.info("用户:{}注册成功，会话id：{}", sourceUserId, sessionId);
                }
                break;

            //用户获取好友操作，返回该用户的所有好友信息
            //需要 sourceUserId
            case INIT_FRIEND_LIST:
                List<UserRecord> userList = friendRecordService.getFriendRecord(sourceUserId);
                TextMessage result = new SocketFriendListData(sourceUserId, userList).getTextMessage();
                log.info(result.toString());
                session.sendMessage(result);
                break;

            case INIT_OFFLINE_CHAT_LIST:
                //发送离线聊天记录
                sendingDeletingDatabaseOfMessages(session,sourceUserId);

            //将用户的聊天记录转发给对应的用户
            // 需要 targetUserId 整个数据包message
                break;
            case TEXT_CHAT:
                forwardMessage(message);
                break;
            case FILE_CHAT:
                break;

            //将用户的SDP和ICE数据包转发给对应的用户
            // 需要 targetUserId 整个数据包message
            case LINK:
                forwardIceAndSdp(targetUserId, message);
                break;

            //添加好友
            //需要sourceUserId，targetUserId，整个数据包
            case ADD_FRIEND:
                break;

            //删除好友
            //需要sourceUserId，targetUserId，整个数据包
            case DELETE_FRIEND:
                break;
        }
    }

    //访问数据库，查看是否存有待发给客户端的消息，若有发送给客户端并且删除已经发送的消息
    public void sendingDeletingDatabaseOfMessages(WebSocketSession session, String sourceUserId) {
        List<ChatRecord> chatRecordList = chatRecordService.getChatRecordByReceiverId(sourceUserId);
        if (!chatRecordList.isEmpty()) {
            try {
                TextMessage result=new SocketChatListData(chatRecordList).getTextMessage();
                session.sendMessage(result);
                chatRecordService.deleteChatRecordByReceiverId(sourceUserId);
                log.info("向{}用户发送离线聊天记录{}", sourceUserId, result);
            } catch (IOException e) {
                log.info(e.getMessage(),e);
                throw new RuntimeException(e);
            }
        }
    }

    // 处理 WebSocket 连接关闭时的行为
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        // 从列表中移除关闭连接的客户端会话
        String clientId = session.getId();
        if (closeStatus.getCode() == 1000) {
            log.info("会话id为：{} 的连接已正常关闭", clientId);
        } else if (closeStatus.getCode() == 1001) {
            log.info("会话id为：{} 的连接关闭了，可能是页面刷新等原因：{}", clientId, closeStatus.getReason());
        } else {
            log.info("WebSocket连接异常关闭，代码：{},原因：{}", clientId, closeStatus.getReason());
        }
        String userId = clientIdList.get(clientId);
        if (userId != null) {
            sessionsList.remove(userId);
            clientIdList.remove(clientId);
        }
    }

    private void forwardMessage(TextMessage message) {
        SocketData socketData = jsonUtil.fromJsonForText(message.getPayload());

        String type = socketData.getType();
        String sourceUserId = socketData.getSourceUserId();
        String targetUserId = socketData.getTargetUserId();
        String content = socketData.getContent();
        Long createAt = socketData.getCreateAt();

        WebSocketSession targetSession = sessionsList.get(targetUserId);

        if (targetSession != null) {
            try {
                targetSession.sendMessage(message);
                log.info("转发给目标用户数据：{}", message.getPayload());
            } catch (IOException e) {
                log.info("发送给目标用户：{} 的数据中断！", targetUserId);
            }
        } else {
            ChatRecord chatRecord = new ChatRecord(sourceUserId, targetUserId, type, content, createAt);
            chatRecordService.saveChatRecord(chatRecord);
        }
    }

    private void forwardIceAndSdp(String targetUserId, TextMessage message) {
        WebSocketSession targetSession = sessionsList.get(targetUserId);
        if (targetSession != null) {
            try {
                targetSession.sendMessage(message);
            } catch (IOException e) {
                log.info("发送给目标用户：{} 的SDO和ICE中断！", targetUserId);
            }
        }


    }

}
