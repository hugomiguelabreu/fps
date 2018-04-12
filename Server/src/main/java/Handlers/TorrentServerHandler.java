package Handlers;

import Network.TorrentWrapperOuterClass;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;

public class TorrentServerHandler extends SimpleChannelInboundHandler<TorrentWrapperOuterClass.TorrentWrapper>{

    private List<Torrent> trackerTorrents;

    public TorrentServerHandler(List<Torrent> trackedTorrentsParam) {
        this.trackerTorrents = trackedTorrentsParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TorrentWrapperOuterClass.TorrentWrapper torrentWrapper) throws Exception {
        Torrent t = new Torrent(torrentWrapper.getContent().toByteArray(), false);
        trackerTorrents.add(t);
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
