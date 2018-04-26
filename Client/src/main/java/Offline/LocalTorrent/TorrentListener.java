package Offline.LocalTorrent;

import Handlers.TorrentListenerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TorrentListener extends Thread{

    private int port;
    private ChannelFuture cf;
    private EventLoopGroup workerGroup;
    private EventLoopGroup bossGroup;
    //private String ownaddress;
    private String ipv4;

    public TorrentListener(String ipv4){

        this.workerGroup = new NioEventLoopGroup();
        this.bossGroup = new NioEventLoopGroup();
        this.ipv4 = ipv4;
        this.port = 0;
    }

    public TorrentListener(String ipv4, int port){

        this.workerGroup = new NioEventLoopGroup();
        this.bossGroup = new NioEventLoopGroup();
        this.ipv4 = ipv4;
        this.port = port;
    }

    public void run(){
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new TorrentListenerInitializer(ipv4));
            cf = b.bind(ipv4,port).sync();
            System.out.println("Torrent Listner inited");
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
