package Handlers;

import Event.ArrayListEvent;
import Network.ClientWrapper;
import com.turn.ttorrent.common.Torrent;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.util.ArrayList;


public class TorrentListenerInitializer extends ChannelInitializer<SocketChannel> {

    private ArrayListEvent<Torrent> available;

    public TorrentListenerInitializer(ArrayListEvent<Torrent> availableParam){
        this.available = availableParam;
    }


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline p = socketChannel.pipeline();
        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(ClientWrapper.ClientMessage.getDefaultInstance()));

        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());
        p.addLast(new TorrentListenerHandler(this.available));

    }
}
