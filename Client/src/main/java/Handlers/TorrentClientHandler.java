package Handlers;

import Network.TorrentWrapperOuterClass;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TorrentClientHandler extends SimpleChannelInboundHandler<TorrentWrapperOuterClass.TorrentWrapper> {

    // Stateful properties
    private volatile Channel channel;

    public TorrentClientHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TorrentWrapperOuterClass.TorrentWrapper torrentWrapper) throws Exception {
        System.out.println("Recebi um novo proto para adicionar");
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        channel = ctx.channel();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
