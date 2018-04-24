package Handlers;


import Network.TorrentWrapperOuterClass;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

public class TorrentListenerHandler extends SimpleChannelInboundHandler<TorrentWrapperOuterClass.TorrentWrapper> {

    public TorrentListenerHandler() {
        super(false);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TorrentWrapperOuterClass.TorrentWrapper torrentWrapper) throws Exception {

        System.out.println("kappa");

    }
}
