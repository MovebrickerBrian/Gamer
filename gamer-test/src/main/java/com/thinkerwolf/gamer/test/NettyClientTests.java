package com.thinkerwolf.gamer.test;

import com.google.protobuf.ByteString;
import com.thinkerwolf.gamer.remoting.tcp.Packet;
import com.thinkerwolf.gamer.netty.tcp.PacketDecoder;
import com.thinkerwolf.gamer.netty.tcp.PacketEncoder;
import com.thinkerwolf.gamer.netty.protobuf.PacketProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.nio.charset.Charset;

public class NettyClientTests {
    public static void main(String[] args) {
        // 106.13.146.85

        startupTcp("106.13.146.85", 8090);
    }

    private static void startupTcp(final String host, final int port) {
        for (int i = 0; i < 1000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bootstrap b = new Bootstrap();
                    b.group(new NioEventLoopGroup(1));
                    b.channel(NioSocketChannel.class);
                    b.handler(getInitializerGamer());
                    final ChannelFuture cf = b.connect(host, port);
                    try {
                        cf.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Object loginPacket = login();
                    cf.channel().writeAndFlush(loginPacket);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }

                    Object packet = getSendMsgGamer();

                    cf.channel().writeAndFlush(packet);

                    try {
                        Thread.sleep(21 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    cf.channel().writeAndFlush(packet);
                }
            }).start();
        }

    }


    private static Object getSendMsgProtobuf() {
       return PacketProto.RequestPacket.newBuilder()
                .setRequestId(1).setCommand("test@jjjc")
                .setContent(ByteString.copyFromUtf8("num=2")).build();
    }

    private static Object login() {
        Packet packet = new Packet();
        packet.setCommand("user@login");
        packet.setRequestId(1);
        packet.setContent("username=wk&password=123".getBytes(Charset.defaultCharset()));
        return packet;
    }

    private static Object getSendMsgGamer() {
        Packet packet = new Packet();
        packet.setCommand("test@jjjc");
        packet.setRequestId(2);
        packet.setContent("num=190".getBytes(Charset.forName("UTF-8")));
        return packet;
    }


    private static ChannelInitializer getInitializerProtobuf() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                ch.pipeline().addLast(new ProtobufEncoder());

                ch.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                ch.pipeline().addLast("decoder", new ProtobufDecoder(PacketProto.ResponsePacket.getDefaultInstance()));

                ch.pipeline().addLast("handler", new SimpleChannelInboundHandler<Object>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                        PacketProto.ResponsePacket packet = (PacketProto.ResponsePacket) msg;
                        System.err.println(packet);
                    }
                });
            }
        };
    }

    private static ChannelInitializer getInitializerGamer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast("encoder", new PacketEncoder());
                ch.pipeline().addLast("decoder", new PacketDecoder());

                ch.pipeline().addLast("handler", new SimpleChannelInboundHandler<Object>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
                        Packet packet = (Packet) msg;
                        System.err.println(new String(packet.getContent()));
                    }
                });
            }
        };
    }

}
