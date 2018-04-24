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
    //private String ownaddress;
    private String ipv6;
    private String ipv4;

    public TorrentListener(String ipv6, String ipv4){

        this.workerGroup = new NioEventLoopGroup();
        this.bossGroup = new NioEventLoopGroup();
        this.ipv6 = ipv6;
        this.ipv4 = ipv4;
    }

    public void run(){
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new TorrentListenerInitializer(ipv4));
            cf = b.bind(ipv6,port).sync();
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
