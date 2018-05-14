package Handlers;

import Network.ClientWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;

public class AutenticationHandler extends SimpleChannelInboundHandler<ClientWrapper.Response> {

    SynchronousQueue<Boolean> queue;


    public AutenticationHandler(SynchronousQueue<Boolean> queue){

        this.queue = queue;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ClientWrapper.Response response) throws Exception {

        System.out.println("repHandler = " + response.getRep());
        queue.put(response.getRep());
    }

}

