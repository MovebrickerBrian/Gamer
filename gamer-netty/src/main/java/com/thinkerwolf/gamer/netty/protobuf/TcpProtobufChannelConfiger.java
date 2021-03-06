package com.thinkerwolf.gamer.netty.protobuf;

import com.thinkerwolf.gamer.common.URL;
import com.thinkerwolf.gamer.netty.concurrent.ConcurrentUtil;
import com.thinkerwolf.gamer.core.servlet.ServletConfig;
import com.thinkerwolf.gamer.netty.ChannelHandlerConfiger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.concurrent.Executor;


/**
 * Tcp handler initializer
 *
 * @author wukai
 */
public class TcpProtobufChannelConfiger extends ChannelHandlerConfiger<Channel> {

    private ServletConfig servletConfig;
    private Executor executor;

    @Override
    public void init(URL url) throws Exception {
        this.executor = ConcurrentUtil.newExecutor(url, "Protobuf-user");
        this.servletConfig = url.getAttach(URL.SERVLET_CONFIG);
    }

    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipe = ch.pipeline();
        ProtobufServerHandler tcpHandler = new ProtobufServerHandler();
        tcpHandler.init(executor, servletConfig);

        pipe.addLast(new ProtobufVarint32FrameDecoder());
        pipe.addLast("decoder", new ProtobufDecoder(PacketProto.RequestPacket.getDefaultInstance()));

        pipe.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipe.addLast("encoder", new ProtobufEncoder());
        pipe.addLast("handler", tcpHandler);
    }


}
