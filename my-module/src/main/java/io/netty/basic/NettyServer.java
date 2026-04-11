package io.netty.basic;

import io.netty.basic.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class NettyServer {
    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1); // 接受连接
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // 处理连接

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 使用NIO
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加编解码器
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringEncoder());
                            // 添加业务处理器
                            pipeline.addLast(new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128) // 连接队列大小
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // 保持连接

            // 绑定端口，开始监听
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("服务器启动，监听端口: " + port);

            // 等待服务器通道关闭
            future.channel().closeFuture().sync();
        } finally {
            // 关闭事件循环组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyServer(8080).start();
    }
}