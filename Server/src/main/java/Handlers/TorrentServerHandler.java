package Handlers;

import Network.Interserver;
import Network.ServerWrapper;
import Util.FileUtils;
import Util.TorrentUtil;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.Map;

public class TorrentServerHandler extends SimpleChannelInboundHandler<ServerWrapper.ServerMessage> {

    private Tracker tck;
    private Map<String, Client> openClients;

    public TorrentServerHandler(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam) {
        this.openClients = openClientsParam;
        this.tck = trackedTorrentsParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ServerWrapper.ServerMessage request) throws Exception {
        Torrent t = new Torrent(request.getTrackerTorrent().getContent().toByteArray(), false);
        //Save torrent for fault sake
        FileUtils.initDir();
        FileUtils.saveTorrent(t);
        //Init a client, so server can get the file
        Client serverCli = TorrentUtil.initClient(t, FileUtils.fileDir);
        openClients.put(t.getHexInfoHash(), serverCli);
        //Get a tracked torrent with observables;
        TrackedTorrent tt =  TorrentUtil.announceTrackedTorrentWithObservers(tck, t, openClients);

        Peer cli = serverCli.getPeerSpec();
        tt.injectPeer(new TrackedPeer(t, cli.getIp(), cli.getPort(), cli.getPeerId()));
        injectionRequest(cli, tt);
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

    private void injectionRequest(Peer peer, TrackedTorrent tt) throws IOException {
        //Inject peer in each of the trackers
        for(URI tracker: tt.getAnnounceList().get(0)) {
            Socket s = new Socket(tracker.getHost(), tracker.getPort());
            Interserver.InterServerMessage im = Interserver.InterServerMessage.newBuilder()
                    .setTypeOp(true)
                    .setServerIp(ByteString.copyFromUtf8(peer.getIp()))
                    .setServerCliPort(peer.getPort())
                    .setTorrentHexId(ByteString.copyFromUtf8(tt.getHexInfoHash()))
                    .build();
            im.writeDelimitedTo(s.getOutputStream());
            s.getOutputStream().flush();
            s.getOutputStream().close();
            s.close();
        }
    }
}