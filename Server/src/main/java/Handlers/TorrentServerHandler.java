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
import org.simpleframework.transport.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TorrentServerHandler extends SimpleChannelInboundHandler<ServerWrapper.ServerMessage> {

    private Tracker tck;
    private Map<String, Client> openClients;
    private ConcurrentHashMap<String, ArrayList<TrackedPeer>> injectionsWaiting;

    public TorrentServerHandler(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam, ConcurrentHashMap<String, ArrayList<TrackedPeer>> injectionsWaitingParam) {
        this.openClients = openClientsParam;
        this.tck = trackedTorrentsParam;
        this.injectionsWaiting = injectionsWaitingParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ServerWrapper.ServerMessage request) throws Exception {
        Torrent t = new Torrent(request.getTrackerTorrent().getContent().toByteArray(), false);
        String group = request.getTrackerTorrent().getGroup();
        //Verificar o nosso local na lista de trackers e colocar
        //o próprio em primeiro lugar
        String ip = FileUtils.getMyIP();
        //Vamos colocar o nosso IP em primeiro lugar;
        int iteration = 0;
        URI uri = null;
        for(URI uriIt: t.getAnnounceList().get(0)){
            if(uriIt.getPort() == tck.getAnnounceUrl().getPort() &&
                    uriIt.getHost().equals(ip)){
                uri = uriIt;
                break;
            }
            iteration++;
        }
        //Redirecionar o torrent para os outros trackers se eu
        //sou o principal
        if(iteration == 0) {
            redirect(t, group);
        }else {
            //Recodificiar se não sou o principal
            t.getAnnounceList().get(0).remove(iteration);
            t.getAnnounceList().get(0).add(0, uri);
            //Volta a codificar novamente o torrent
            t.newAnnounceEncode();
        }

        //Save torrent for fault sake
        FileUtils.saveTorrent(t);
        //Init a client, so server can get the file
        Client serverCli = TorrentUtil.initClient(t, FileUtils.fileDir);
        openClients.put(t.getHexInfoHash(), serverCli);
        //Get a tracked torrent with observables;
        TrackedTorrent tt =  TorrentUtil.announceTrackedTorrentWithObservers(tck, t, openClients);

        //Obtem o peer local e define-o para
        //recebermos os updates de users normais também
        Peer cli = serverCli.getPeerSpec();
        tt.setlocalInjectPeerID(cli.getHexPeerId());
        //Efetuar o pedido de injeção aos outros servers e injetar os pendentes
        TorrentUtil.injectionRequest(cli, injectionsWaiting, tt);
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

    private void redirect(Torrent t, String group) throws IOException {
        List<URI> trackers = t.getAnnounceList().get(0);
        //Remover-me
        trackers.remove(0);
        //Redirecionar
        for(URI tracker: trackers) {
            Socket s = new Socket(tracker.getHost(), 5000);
            ServerWrapper.TrackerTorrent tt = ServerWrapper.TrackerTorrent.newBuilder()
                    .setContent(ByteString.copyFrom(t.getEncoded()))
                    .setGroup(group)
                    .build();
            ServerWrapper.ServerMessage im = ServerWrapper.ServerMessage.newBuilder()
                    .setTrackerTorrent(tt)
                    .build();
            im.writeDelimitedTo(s.getOutputStream());
            s.getOutputStream().flush();
            s.getOutputStream().close();
            s.close();
        }
    }

}