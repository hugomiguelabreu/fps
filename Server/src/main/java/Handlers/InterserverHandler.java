package Handlers;

import Network.Interserver;
import Util.FileUtils;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.common.Utils;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

public class InterserverHandler extends SimpleChannelInboundHandler<Interserver.InterServerMessage> {

    private Tracker tck;
    private Map<String, Client> openClients;
    private Map<String, ArrayList<TrackedPeer>> injectionsWaiting;
    private Map<String, ArrayList<TrackedPeer>> deletionsWaiting;

    public InterserverHandler(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam,
                              Map<String, ArrayList<TrackedPeer>> injectionsWaitingParam,
                              Map<String, ArrayList<TrackedPeer>> deletionsWaitingParam) {
        this.openClients = openClientsParam;
        this.injectionsWaiting = injectionsWaitingParam;
        this.deletionsWaiting = deletionsWaitingParam;
        this.tck = trackedTorrentsParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Interserver.InterServerMessage message) throws Exception {
        boolean type = message.getTypeOp();
        String ip = message.getServerIp().toStringUtf8();
        int port = message.getServerCliPort();
        String torrentId = message.getTorrentHexId().toStringUtf8();
        String peerId = message.getPeerId().toStringUtf8();
        String group = message.getGroup();

        if(type){
            System.out.println("Handle peer injection");
            if(openClients.containsKey(torrentId)){
                System.out.println("TENHO");
                //We're handling that torrent.
                TrackedTorrent tt =  tck.getTrackedTorrents().stream().peek(x -> x.getHexInfoHash().equals(torrentId)).findFirst().get();
                //PEERS SUPER NODOS
                TrackedPeer tpInj = new TrackedPeer(tt, ip, port, ByteBuffer.wrap(peerId.getBytes(Torrent.BYTE_ENCODING)));
                System.out.println(tpInj.getIp());
                System.out.println(tpInj.getPort());
                System.out.println(tpInj.getHexPeerId());
                System.out.println(tt.getHexInfoHash());
                tt.injectPeer(tpInj);
            }else{
                System.out.println("NAO TENHO");
                //Ainda não temos esse torrent, mas supostamente vamos receber.
                if(!injectionsWaiting.containsKey(torrentId) || injectionsWaiting.get(torrentId) == null)
                    injectionsWaiting.put(torrentId, new ArrayList<>());
                //Novo server que também vai querer e tem o ficheiro
                injectionsWaiting.get(torrentId).add(new TrackedPeer(null, ip, port, ByteBuffer.wrap(peerId.getBytes(Torrent.BYTE_ENCODING))));
            }
        }else{
            if(!group.equals("")){
                TrackedTorrent delete = null;
                for(TrackedTorrent tt: tck.getTrackedTorrents())
                    if(tt.getHexInfoHash().equals(torrentId))
                        delete = tt;
                if(delete != null){
                    tck.remove(delete);
                    TrackedTorrent finalDelete = delete;
                    new Thread(() -> {
                        try {
                            FileUtils.deleteTorrent(finalDelete, group);
                            FileUtils.deleteFiles(finalDelete);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }else{
                System.out.println("Handle peer deletion");
                if(!deletionsWaiting.containsKey(torrentId) || deletionsWaiting.get(torrentId) == null)
                    deletionsWaiting.put(torrentId, new ArrayList<>());
                TrackedPeer deleteadd = new TrackedPeer(null, ip, port, ByteBuffer.wrap(peerId.getBytes(Torrent.BYTE_ENCODING)));
                if(deletionsWaiting.get(torrentId).stream().anyMatch(x -> x.getHexPeerId().equals(deleteadd.getHexPeerId()))){
                    System.out.println("Remove injection duplicated");
                }else{
                    deletionsWaiting.get(torrentId).add(deleteadd);
                }
            }
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
