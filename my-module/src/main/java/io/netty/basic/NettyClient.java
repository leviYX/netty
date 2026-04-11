package io.netty.basic;

import io.netty.basic.handler.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class NettyClient {

    private static final Logger LOG = LoggerFactory.getLogger(NettyClient.class);

    private String host;
    private int port;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加编解码器
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            // 添加业务处理器
                            pipeline.addLast(new ClientHandler());
                        }
                    });

            // 连接到服务器
            ChannelFuture connect = bootstrap.connect(host, port);
            connect.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                       future.channel().writeAndFlush(Unpooled.copiedBuffer("Hello server,I am client,I have call listener", CharsetUtil.UTF_8));
                    } else {
                        LOG.error("Failed to connect to {}:{}", host, port, future.cause());
                    }
                }
            });
            ChannelFuture future = connect.sync();
            System.out.println("连接到服务器: " + host + ":" + port);

            Channel channel = future.channel();

            // 启动输入线程，用于发送消息
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if ("quit".equalsIgnoreCase(line)) {
                    break;
                }
                channel.writeAndFlush(line);
            }

            // 等待连接关闭
            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyClient("localhost", 8080).start();
    }
}
