package com.thinkerwolf.gamer.netty.tcp.gamer;

import com.thinkerwolf.gamer.core.decorator.Decorator;
import com.thinkerwolf.gamer.core.model.Model;
import com.thinkerwolf.gamer.core.servlet.Request;
import com.thinkerwolf.gamer.core.servlet.Response;

public class TcpDecorator implements Decorator {

    @Override
    public Object decorate(Model<?> model, Request request, Response response) {
        byte[] bytes = model.getBytes();
        Packet packet = new Packet();
        packet.setOpcode((Integer) response.getContentType());
        packet.setRequestId(request.getRequestId());
        packet.setCommand(request.getCommand());
        packet.setContent(bytes);
        return packet;
    }
}
