package Handlers;

import Network.Interserver;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

public class InterserverHandler extends SimpleChannelInboundHandler<Interserver.InterServerMessage> {

    private Tracker tck;
    private Map<String, Client> openClients;
    private Map<String, ArrayList<TrackedPeer>> injectionsWaiting;

    public InterserverHandler(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam, Map<String, ArrayList<TrackedPeer>> injectionsWaitingParam) {
        this.openClients = openClientsParam;
        this.injectionsWaiting = injectionsWaitingParam;
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

        if(type){
            if(openClients.containsKey(torrentId)){
                //We're handling that torrent.
                TrackedTorrent tt =  tck.getTrackedTorrents().stream().peek(x -> x.getHexInfoHash().equals(torrentId)).findFirst().get();
                //PEERS SUPER NODOS
                tt.injectPeer(new TrackedPeer(tt, ip, port, ByteBuffer.wrap(peerId.getBytes(Torrent.BYTE_ENCODING))));
            }else{
                //Ainda não temos esse torrent, mas supostamente vamos receber.
                if(injectionsWaiting.get(torrentId) == null)
                    injectionsWaiting.put(torrentId, new ArrayList<>());
                //Novo server que também vai querer e tem o ficheiro
                injectionsWaiting.get(torrentId).add(new TrackedPeer(null, ip, port, ByteBuffer.wrap(peerId.getBytes(Torrent.BYTE_ENCODING))));
            }
        }else{
            TrackedTorrent tt =  tck.getTrackedTorrents().stream().peek(x -> x.getHexInfoHash().equals(torrentId)).findFirst().get();
            tt.removeInjectedPeer(peerId);
            //Um servidor eliminou o ficheiro.
            //TODO: DELETE
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
