package Handlers;

import Network.Interserver;
import Network.ServerWrapper;
import Util.FileUtils;
import Util.TorrentUtil;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
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
        //Verificar o nosso local na lista de trackers e colocar
        //o próprio em primeiro lugar
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));
        String ip = in.readLine(); //you get the IP as a String
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
        t.getAnnounceList().get(0).remove(iteration);
        t.getAnnounceList().get(0).add(0, uri);
        //Volta a codificar novamente o torrent
        t.newAnnounceEncode();

        //Save torrent for fault sake
        FileUtils.initDir();
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
            Socket s = new Socket(tracker.getHost(), 6000);
            Interserver.InterServerMessage im = Interserver.InterServerMessage.newBuilder()
                    .setTypeOp(true)
                    .setServerIp(ByteString.copyFromUtf8(peer.getIp()))
                    .setServerCliPort(peer.getPort())
                    .setTorrentHexId(ByteString.copyFromUtf8(tt.getHexInfoHash()))
                    .setPeerId(ByteString.copyFromUtf8(new String(peer.getPeerId().array())))
                    .build();
            im.writeDelimitedTo(s.getOutputStream());
            s.getOutputStream().flush();
            s.getOutputStream().close();
            s.close();
        }
    }
}