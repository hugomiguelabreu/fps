package Handlers;


import Network.TorrentWrapperOuterClass;
import com.turn.ttorrent.common.Torrent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.ArrayList;

public class TorrentListenerHandler extends SimpleChannelInboundHandler<TorrentWrapperOuterClass.TorrentWrapper> {

    private ArrayList<Torrent> available;

    public TorrentListenerHandler(ArrayList<Torrent> availableParam) {
        super(false);
        this.available = availableParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TorrentWrapperOuterClass.TorrentWrapper torrentWrapper) throws Exception {
        //TODO: Check if all of them will seed
        Torrent t = new Torrent(
                torrentWrapper.getContent().toByteArray(),
                true);
        available.add(t);
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