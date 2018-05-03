package Handlers;

import Network.TorrentWrapperOuterClass;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import java.util.Map;


public class TorrentServerInitializer extends ChannelInitializer<SocketChannel>{

    private Tracker trackedTorrents;
    private Map<String, Client> openClients;

    public TorrentServerInitializer(Tracker trackedTorrentsParam, Map<String, Client> openClientsParam){
        this.openClients = openClientsParam;
        this.trackedTorrents = trackedTorrentsParam;
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();

        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(TorrentWrapperOuterClass.TorrentWrapper.getDefaultInstance()));
        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());
        p.addLast(new TorrentServerHandler(trackedTorrents, openClients));
    }
}
