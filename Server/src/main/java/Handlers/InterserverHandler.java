package Handlers;

import Network.Interserver;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.ByteBuffer;
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
        boolean type = message.getTypeOp();
        String ip = message.getServerIp().toStringUtf8();
        int port = message.getServerCliPort();
        String torrentId = message.getTorrentHexId().toStringUtf8();
        String peerId = message.getPeerId().toStringUtf8();

        if(openClients.containsKey(torrentId)){
            //We're handling that torrent.
            Peer localCli = openClients.get(torrentId).getPeerSpec();
            TrackedTorrent tt =  tck.getTrackedTorrents().stream().peek(x -> x.getHexInfoHash().equals(torrentId)).findFirst().get();
            //PEERS SUPER NODOS
            //TODO: tratar do id dos peers
            tt.injectPeer(new TrackedPeer(tt, ip, port, ByteBuffer.wrap(peerId.getBytes(Torrent.BYTE_ENCODING))));
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
