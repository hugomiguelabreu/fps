package Handlers;

import Network.TorrentWrapperOuterClass;
import Util.FileUtils;
import Util.TorrentUtil;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.Map;

public class TorrentServerHandler extends SimpleChannelInboundHandler<TorrentWrapperOuterClass.TorrentWrapper>{

    private Tracker tck;
    private Map<String, Client> openClients;

    public TorrentServerHandler(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam) {
        super(false);
        this.openClients = openClientsParam;
        this.tck = trackedTorrentsParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TorrentWrapperOuterClass.TorrentWrapper torrentWrapper) throws Exception {
        Torrent t = new Torrent(torrentWrapper.getContent().toByteArray(), false);
        //Save torrent for fault sake
        FileUtils.initDir();
        FileUtils.saveTorrent(t);
        //Get a tracked torrent with observables;
        TrackedTorrent tt = TorrentUtil.getTrackedTorrentWithObservers(t, openClients);
        //Start tracked torrent and client
        //No need to check if torrent is already announced
        tck.announce(tt);
        //Init a client, so server can get the file
        Client serverCli = TorrentUtil.initClient(t, "/tmp");
        openClients.put(t.getHexInfoHash(), serverCli);
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