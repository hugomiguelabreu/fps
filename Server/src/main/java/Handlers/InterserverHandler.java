package Handlers;

import Network.Interserver;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Map;

public class InterserverHandler extends SimpleChannelInboundHandler<Interserver.InterServerMessage> {

    private Tracker tck;
    private Map<String, Client> openClients;

    public InterserverHandler(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam) {
        this.openClients = openClientsParam;
        this.tck = trackedTorrentsParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Interserver.InterServerMessage message) throws Exception {
        System.out.println("Handle peer injection or Deletion");
        System.out.println(message.getTypeOp());
        System.out.println(message.getServerIp().toStringUtf8());
        System.out.println(message.getServerCliPort());
        String torrentId = message.getTorrentHexId().toStringUtf8();
        System.out.println(torrentId);
        if(openClients.containsKey(torrentId)){
            //We're handling that torrent.
            Peer localCli = openClients.get(torrentId).getPeerSpec();
            TrackedTorrent tt =  tck.getTrackedTorrents().stream().peek(x -> x.getHexInfoHash().equals(torrentId)).findFirst().get();
            System.out.println(tt.getHexInfoHash());
            //PEERS SUPER NODOS
            //tt.injectPeer(new TrackedPeer(t, "192.168.90.90", cli.getPort(), ByteBuffer.wrap("asdasd".getBytes(Torrent.BYTE_ENCODING))));
        }
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
