package Offline.LocalTorrent;

import Handlers.TorrentClientHandler;
import Handlers.TorrentListenerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TorrentListener extends Thread{

    private final int port = 5558;
    private ChannelFuture cf;
    private EventLoopGroup workerGroup;
    private EventLoopGroup bossGroup;
    private String ownaddress;

    public TorrentListener(String ownaddress){

        this.workerGroup = new NioEventLoopGroup();
        this.bossGroup = new NioEventLoopGroup();
        this.ownaddress = ownaddress;
    }

    public void run(){
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new TorrentListenerInitializer());
            cf = b.bind(ownaddress,port).sync();
            System.out.println("Server initiated.");
            //Wait for channel to close
            cf.channel().closeFuture().sync();
            System.out.println("Server shutting down.");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
