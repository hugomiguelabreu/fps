package Handlers;


import Event.ArrayListEvent;
import Util.FileUtils;
import Network.ClientWrapper;
import com.turn.ttorrent.common.Torrent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.ArrayList;

public class TorrentListenerHandler extends SimpleChannelInboundHandler<ClientWrapper.ClientMessage> {

    private ArrayListEvent<Torrent> available;

    public TorrentListenerHandler(ArrayListEvent<Torrent> availableParam) {
        super(false);
        this.available = availableParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ClientWrapper.ClientMessage torrentWrapper) throws Exception {
        //TODO: Check if all of them will seed

        Torrent t = new Torrent(
                torrentWrapper.getTorrentWrapper().getContent().toByteArray(),
                true);
        String group = torrentWrapper.getTorrentWrapper().getGroup();
        available.addTorrent(t);
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
