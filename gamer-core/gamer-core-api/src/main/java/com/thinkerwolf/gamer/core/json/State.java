package com.thinkerwolf.gamer.core.json;

/**
 * 接口状态
 *
 * @author wukai
 */
@Deprecated
public enum State {
    /**
     * 异常
     */
    EXCEPTION(0),
    /**
     * 成功
     */
    SUCCESS(1),
    /**
     * 失败
     */
    FAIL(2),
    /**
     * 推送
     */
    PUSH(3),
    /**
     * 未登录或者session过期
     */
    NO_LOGIN(4),

    ;
    int id;

    State(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
