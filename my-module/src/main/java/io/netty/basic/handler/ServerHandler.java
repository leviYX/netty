package io.netty.basic.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端连接: " + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;
        System.out.println("收到客户端消息: " + message);

        // 向客户端回复
        String response = "服务器收到: " + message + " (时间: " + System.currentTimeMillis() + ")";
        ctx.writeAndFlush(response);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("发生异常: " + cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端断开连接: " + ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }
}
