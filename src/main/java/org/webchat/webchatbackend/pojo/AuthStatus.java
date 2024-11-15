package org.webchat.webchatbackend.pojo;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AuthStatus {

    // 登录成功状态
    LOGIN_SUCCESS(100, "登录成功"),

    // 登录失败：用户名或密码错误
    LOGIN_FAIL_INVALID_CREDENTIALS(101, "用户名或密码错误"),

    // 注册成功状态
    REGISTER_SUCCESS(200, "注册成功"),

    // 注册失败：用户已存在
    REGISTER_FAIL_USERNAME_EXISTS(201, "用户已存在");

//    // 注册失败：邮箱已被注册
//    REGISTER_FAIL_EMAIL_EXISTS(202, "邮箱已被注册"),
//
//    // 注册失败：密码不符合要求
//    REGISTER_FAIL_PASSWORD_WEAK(203, "密码不符合要求");

    private final Integer code;
    private final String message;

    AuthStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @JsonValue
    public Integer getCode() {
        return code;
    }

    @JsonValue
    public String getMessage() {
        return message;
    }

    // 根据 code 获取对应的 message
    public static String getMessageByCode(Integer code) {
        for (AuthStatus status : AuthStatus.values()) {
            if (status.getCode().equals(code)) {
                return status.getMessage();
            }
        }
        return "未知错误";
    }
}
