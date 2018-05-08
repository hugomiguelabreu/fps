package Handlers;

import Network.TorrentWrapperOuterClass;
import Network.Wrapper;
import Util.FileUtils;
import Util.TorrentUtil;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.Map;

public class TorrentServerHandler extends SimpleChannelInboundHandler<Wrapper.TorrentWrapper>{

    private Tracker tck;
    private Map<String, Client> openClients;

    public TorrentServerHandler(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam) {
        super(false);
        this.openClients = openClientsParam;
        this.tck = trackedTorrentsParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Wrapper.TorrentWrapper torrentWrapper) throws Exception {
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