package Handlers;

import Util.FileUtils;
import Util.TorrentUtil;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import server_network.ServerWrapper;

import java.util.Map;

public class TorrentServerHandler extends SimpleChannelInboundHandler<ServerWrapper.TrackerTorrent> {

    private Tracker tck;
    private Map<String, Client> openClients;

    public TorrentServerHandler(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam) {
        this.openClients = openClientsParam;
        this.tck = trackedTorrentsParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ServerWrapper.TrackerTorrent torrentWrapper) throws Exception {
        Torrent t = new Torrent(torrentWrapper.getContent().toByteArray(), false);
        //Save torrent for fault sake
        FileUtils.initDir();
        FileUtils.saveTorrent(t);
        //Init a client, so server can get the file
        Client serverCli = TorrentUtil.initClient(t, FileUtils.fileDir);
        openClients.put(t.getHexInfoHash(), serverCli);
        //Get a tracked torrent with observables;
        TorrentUtil.announceTrackedTorrentWithObservers(tck, t, openClients);
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