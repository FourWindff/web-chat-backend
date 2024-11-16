package org.webchat.webchatbackend.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    @Value("${file.file-path}")
    private String fileDirectory;
    //用户id-会话
    private final ConcurrentHashMap<String, WebSocketSession> sessionsList = new ConcurrentHashMap<>();
    //会话id-用户id
    private final ConcurrentHashMap<String, String> userIdList = new ConcurrentHashMap<>();
    //用户id-文件名
    private final ConcurrentHashMap<String, String> userUpLoadFileName = new ConcurrentHashMap<>();


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
        // 设置文本消息的大小限制（字节）
        session.setTextMessageSizeLimit(1048576);
        // 设置二进制消息的大小限制（字节）
        session.setBinaryMessageSizeLimit(1048576);

    }


    // 处理接收到的消息 type="login" “registry” "chat" "link"
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
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
        String content = socketData.getContent();
        Long createAt = socketData.getCreateAt();
        Integer size = socketData.getSize();


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
                        userIdList.put(sessionId, sourceUserId);
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
                        userIdList.put(sessionId, sourceUserId);
                    }
                    log.info("用户:{}注册成功，会话id：{}", sourceUserId, sessionId);
                }
                break;


            //用户获取好友操作，返回该用户的所有好友信息
            //需要 sourceUserId
            case INIT_FRIEND_LIST:
                List<UserRecord> userList = friendRecordService.getFriendRecord(sourceUserId);
                TextMessage result = new SocketFriendListData(sourceUserId, userList).getTextMessage();
                log.info(result.getPayload());
                session.sendMessage(result);
                break;


            //用户获取离线时未接收的所有消息，
            //需要 sourceUserId
            case INIT_OFFLINE_CHAT_LIST:
                //发送离线聊天记录
                sendingDeletingDatabaseOfMessages(session, sourceUserId);
                break;


            //将用户的聊天记录转发给对应的用户
            // 需要 整个数据包message
            case TEXT_CHAT:
                forwardMessage(message);
                break;


            //将用户的文件发送记录转发给对应的用户，让用户根据里面的文件名在本地上找并渲染在chat组件
            //需要 整个数据包
            case FILE_CHAT:
                if (content.isEmpty()) {
                    log.info("上传的文件名为空");
                    return;
                } else if (userUpLoadFileName.containsKey(sourceUserId)) {
                    log.info("请等待之前的文件上传完毕");
                    return;
                }
                chatRecordService.saveChatRecord(
                        new ChatRecord(
                                sourceUserId,
                                targetUserId,
                                type.getType(),
                                content,
                                createAt,
                                size
                        ));
                //用户正在上传的文件
                userUpLoadFileName.put(sourceUserId, content);

                forwardMessage(message);
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

    //用户上传文件，在一个文件上传之前用户不能再次上传
    //后面可以设置禁用用户的上传按钮
    @Override
    public void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        String sourceUserId = userIdList.getOrDefault(session.getId(), null);
        String fileName = userUpLoadFileName.getOrDefault(sourceUserId, null);
        // 获取 BinaryMessage 的 ByteBuffer
        ByteBuffer payload = message.getPayload();

        // 获取字节数量
        int size = payload.remaining();

        if (fileName == null) {
            log.error("未找到文件名，无法保存文件");
            return;
        }
        // 设置文件保存路径
        Path directoryPath = Paths.get(fileDirectory);

        // 确保目录存在
        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
        } catch (IOException e) {
            log.error("创建文件夹失败: {}", directoryPath);
            log.error(e.getMessage());
            return;
        }

        // 设置完整的文件路径（包括文件名）
        Path filePath = directoryPath.resolve(fileName);

        // 将接收到的二进制数据写入文件
        try {
            Files.write(filePath, message.getPayload().array(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("保存文件失败: {}", e.getMessage());
            log.error(e.getMessage());
        }
        if (size < 1024 * 1024) {
            userUpLoadFileName.remove(sourceUserId);
            log.info("文件已保存至: {}", filePath);
            //开始转发
            forwardBinary(sourceUserId);
        }

    }

    //访问数据库，查看是否存有待发给客户端的消息，若有发送给客户端并且删除已经发送的消息
    public void sendingDeletingDatabaseOfMessages(WebSocketSession session, String sourceUserId) {
        //转发文字聊天和文件聊天记录
        List<ChatRecord> chatRecordList = chatRecordService.getChatRecordByReceiverId(sourceUserId);
        if (!chatRecordList.isEmpty()) {
            try {
                TextMessage result = new SocketChatListData(chatRecordList).getTextMessage();
                session.sendMessage(result);
                chatRecordService.deleteChatRecordByReceiverId(sourceUserId);
                log.info("向{}用户发送离线聊天记录{}", sourceUserId, result);
            } catch (IOException e) {
                log.info(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        }
        //转发文件本身
        forwardBinary(sourceUserId);
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
        String userId = userIdList.get(clientId);
        if (userId != null) {
            sessionsList.remove(userId);
            userIdList.remove(clientId);
        }
    }

    //转发TEXT_CHAT和FILE_CHAT类型的消息
    //FILE_TEXT转发不删除,发送文件时才删除
    //TEXT_TEXT转发就删除
    private void forwardMessage(TextMessage message) {
        SocketData socketData = jsonUtil.fromJsonForText(message.getPayload());

        String type = socketData.getType();
        String sourceUserId = socketData.getSourceUserId();
        String targetUserId = socketData.getTargetUserId();
        String content = socketData.getContent();
        Long createAt = socketData.getCreateAt();
        Integer size = socketData.getSize();

        WebSocketSession targetSession = sessionsList.get(targetUserId);

        if (targetSession != null) {
            try {
                targetSession.sendMessage(message);
                log.info("转发给目标用户数据：{}", message.getPayload());
            } catch (IOException e) {
                log.info("发送给目标用户：{} 的数据中断！", targetUserId);
            }
        } else {
            //如果对方不在线保存文字聊天记录，文件聊天记录都会保存，直到被发送过去
            if(SocketDataType.TEXT_CHAT.getType().equals(type)) {
                ChatRecord chatRecord = new ChatRecord(sourceUserId, targetUserId, type, content, createAt,size );
                chatRecordService.saveChatRecord(chatRecord);
            }
        }
    }

    private void forwardBinary(String sourceUserId) {
        //在此之前已经插入了messageType为FILE_CHAT类型的记录
        List<ChatRecord> binaryFileList = chatRecordService.getFileRecordBySenderId(sourceUserId);
        if (!binaryFileList.isEmpty()) {
            binaryFileList.forEach(chatRecord -> {
                Integer id = chatRecord.getId();
                String targetUserId = chatRecord.getReceiverId();
                String fileName = chatRecord.getMessageContent();
                WebSocketSession targetSession = sessionsList.getOrDefault(targetUserId, null);
                //对方在线
                if (targetSession != null) {
                    boolean isSend = sendFileToUser(targetSession, fileName);
                    if (isSend) {
                        log.info("文件已转发给{}", sourceUserId);
                        chatRecordService.deleteChatRecordById(id);
                    }
                }
            });
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

    // 发送文件到目标用户
    private boolean sendFileToUser(WebSocketSession targetSession, String fileName) {
        // 获取文件数据，假设文件存储路径是通过 fileName 来获得
        Path filePath = Paths.get(fileDirectory, fileName);
        try {
            // 检查文件是否存在
            if (Files.exists(filePath)) {
                long fileSize = Files.size(filePath); // 获取文件大小
                int chunkSize = 1024 * 1024;  // 设定每次发送的块大小（例如 1MB）
                long totalChunks = (fileSize + chunkSize - 1) / chunkSize;  // 计算总块数

                log.info("开始分块发送文件 {}，文件大小：{}，共分为 {} 块", fileName, fileSize, totalChunks);

                try (InputStream fileInputStream = Files.newInputStream(filePath)) {
                    byte[] buffer = new byte[chunkSize];
                    int bytesRead;
                    int chunkIndex = 0;

                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        // 读取到的字节数据
                        byte[] chunkData = Arrays.copyOf(buffer, bytesRead); // 只取有效字节
                        // 创建二进制消息并发送
                        BinaryMessage binaryMessage = new BinaryMessage(chunkData);
                        targetSession.sendMessage(binaryMessage); // 发送当前块数据
                        chunkIndex++;
                        log.info("已发送第 {} 块，文件 {}，大小：{} 字节", chunkIndex, fileName, bytesRead);
                    }
                }
                log.info("文件 {} 已全部发送完毕", fileName);
                return true;
            } else {
                log.error("文件 {} 不存在", fileName);
            }
        } catch (IOException e) {
            log.error("发送文件失败: {}", e.getMessage());
        }
        return false;
    }
}
