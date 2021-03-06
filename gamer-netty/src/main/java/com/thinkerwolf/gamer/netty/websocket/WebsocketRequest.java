package com.thinkerwolf.gamer.netty.websocket;

import com.thinkerwolf.gamer.core.servlet.Protocol;
import com.thinkerwolf.gamer.core.servlet.ServletContext;
import com.thinkerwolf.gamer.core.servlet.Session;
import com.thinkerwolf.gamer.core.servlet.SessionManager;
import com.thinkerwolf.gamer.core.util.RequestUtil;
import com.thinkerwolf.gamer.netty.util.InternalHttpUtil;
import com.thinkerwolf.gamer.netty.AbstractRequest;
import io.netty.channel.Channel;

public class WebsocketRequest extends AbstractRequest {

    private final Channel channel;

    private final byte[] content;

    private final ServletContext servletContext;

    public WebsocketRequest(int requestId, String command, Channel channel, byte[] content, ServletContext servletContext) {
        super(requestId, command, channel);
        this.channel = channel;
        this.servletContext = servletContext;
        this.content = content;
        RequestUtil.parseParams(this, content);

        Session session = getSession(false);
        if (session != null) {
            session.setPush(new WebsocketPush(channel));
        }
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public Session getSession() {
        return getSession(false);
    }

    @Override
    public Session getSession(boolean create) {
        SessionManager sessionManager = servletContext.getSessionManager();
        if (sessionManager == null) {
            return null;
        }
        String sessionId = getInternalSessionId();
        Session session = sessionManager.getSession(sessionId, create);
        if (create && session != null && !session.getId().equals(sessionId)) {
            // 过期或者创建新session
            session.setPush(new WebsocketPush(channel));
            channel.attr(InternalHttpUtil.CHANNEL_JSESSIONID).set(session.getId());
        }
        if (session != null) {
            session.touch();
        }
        return session;
    }

    private String getInternalSessionId() {
        if (channel.hasAttr(InternalHttpUtil.CHANNEL_JSESSIONID)) {
            return channel.attr(InternalHttpUtil.CHANNEL_JSESSIONID).toString();
        }
        String sessionId = (String) getAttribute(Session.JSESSION);
        return sessionId;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.WEBSOCKET;
    }
}
