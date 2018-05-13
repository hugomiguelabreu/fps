package Handlers;

import Util.FileUtils;
import Util.TorrentUtil;
import com.google.protobuf.ByteString;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.common.Peer;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.xml.sax.SAXException;
import server_network.Interserver;
import server_network.ServerWrapper;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public class ServerHandler extends SimpleChannelInboundHandler<Interserver.Message> {

    private Tracker tck;
    private Map<String, Client> openClients;

    public ServerHandler(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam) {
        this.openClients = openClientsParam;
        this.tck = trackedTorrentsParam;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Interserver.Message request) throws Exception {
        if(request.hasSm()){
            Interserver.ServerMessage sm = request.getSm();
            this.handleInterserverRequest(sm);
        }else{
            Interserver.TorrentMessage tm = request.getTm();
            this.handleTorrent(tm);
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

    private void handleTorrent(Interserver.TorrentMessage torrentProto) throws IOException, NoSuchAlgorithmException, InterruptedException, SAXException, ParserConfigurationException {
        Torrent t = new Torrent(torrentProto.getContent().toByteArray(), false);
        //Save torrent for fault sake
        FileUtils.initDir();
        FileUtils.saveTorrent(t);
        //Init a client, so server can get the file
        Client serverCli = TorrentUtil.initClient(t, FileUtils.fileDir);
        openClients.put(t.getHexInfoHash(), serverCli);
        //Get a tracked torrent with observables;
        TrackedTorrent tt =  TorrentUtil.announceTrackedTorrentWithObservers(tck, t, openClients);

        //Sends GET to other SERVERS
        NioEventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
            .channel(NioSocketChannel.class)
            .handler(new ServerHandlerInitializer(tck, openClients));
        // Make a new connection.
        Channel ch = b.connect("localhost", 5001).sync().channel();
        Interserver.ServerMessage sw = Interserver.ServerMessage.newBuilder()
                .setTypeOp(true)
                .setServerIp(ByteString.copyFromUtf8(serverCli.getPeerSpec().getIp()))
                .setServerCliPort(serverCli.getPeerSpec().getPort())
                .setTorrentHexId(ByteString.copyFromUtf8(tt.getHexInfoHash()))
                .build();
        Interserver.Message im = Interserver.Message.newBuilder().setSm(sw).build();
        //Escreve e espera pela escrita no socket
        ch.writeAndFlush(im).sync();
        ch.close().sync();
    }

    private void handleInterserverRequest(Interserver.ServerMessage sm){

        System.out.println("Handle peer injection or Deletion");
        System.out.println(sm.getTypeOp());
        System.out.println(sm.getServerIp().toStringUtf8());
        System.out.println(sm.getServerCliPort());
        System.out.println(sm.getTorrentHexId().toStringUtf8());
        //Peer cli = serverCli.getPeerSpec();
        //PEERS SUPER NODOS
        //tt.injectPeer(new TrackedPeer(t, cli.getIp(), cli.getPort(), cli.getPeerId())); INJETA-SE A SI PRÓPRIO?
        //tt.injectPeer(new TrackedPeer(t, "192.168.90.90", cli.getPort(), ByteBuffer.wrap("asdasd".getBytes(Torrent.BYTE_ENCODING))));
        //tt.injectPeer(new TrackedPeer(t, "192.168.90.91", cli.getPort(), ByteBuffer.wrap("asdasdasdasdasdas".getBytes(Torrent.BYTE_ENCODING))));
    }

}