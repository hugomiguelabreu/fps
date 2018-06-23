package Handlers;

import Network.Interserver;
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

public class InterserverInitializer extends ChannelInitializer<SocketChannel>{

    private Tracker trackedTorrents;
    private Map<String, Client> openClients;
    private Map<String, ArrayList<TrackedPeer>> injectionsWaiting;

    public InterserverInitializer(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam, Map<String, ArrayList<TrackedPeer>> injectionsWaitingParam){
        this.openClients = openClientsParam;
        this.injectionsWaiting = injectionsWaitingParam;
        this.trackedTorrents = trackedTorrentsParam;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(Interserver.InterServerMessage.getDefaultInstance()));
        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());
        p.addLast(new InterserverHandler(this.trackedTorrents, this.openClients, this.injectionsWaiting));
    }
}
