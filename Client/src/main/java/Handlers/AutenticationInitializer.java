package Handlers;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import Network.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;

public class AutenticationInitializer extends ChannelInitializer<SocketChannel>{

    private SynchronousQueue<Boolean> queue;

    public AutenticationInitializer(SynchronousQueue<Boolean> queue ){

        this.queue = queue;
    }


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {

        ChannelPipeline p = socketChannel.pipeline();
        p.addLast(new ProtobufVarint32FrameDecoder());
        p.addLast(new ProtobufDecoder(ClientWrapper.Response.getDefaultInstance()));

        p.addLast(new ProtobufVarint32LengthFieldPrepender());
        p.addLast(new ProtobufEncoder());
        p.addLast(new AutenticationHandler(queue));

    }
}
