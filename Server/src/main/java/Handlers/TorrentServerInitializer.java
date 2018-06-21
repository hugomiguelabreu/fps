package Handlers;

import Network.ServerWrapper;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.tracker.TrackedPeer;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TorrentServerInitializer extends ChannelInitializer<SocketChannel>{

    private Tracker trackedTorrents;
    private Map<String, Client> openClients;
    private ConcurrentHashMap<String, ArrayList<TrackedPeer>> injectionsWaiting;

    public TorrentServerInitializer(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam, ConcurrentHashMap<String, ArrayList<TrackedPeer>> injectionsWaitingParam){
        this.openClients = openClientsParam;
        this.injectionsWaiting = injectionsWaitingParam;
        this.trackedTorrents = trackedTorrentsParam;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(ServerWrapper.ServerMessage.getDefaultInstance()));
        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());
        p.addLast(new TorrentServerHandler(this.trackedTorrents, this.openClients, this.injectionsWaiting));
    }
}
