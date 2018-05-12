package Handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server_network.Interserver;

public class ServerInfoHandler extends SimpleChannelInboundHandler<Interserver> {

    public ServerInfoHandler(){

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Interserver serverRequest) throws Exception {

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
