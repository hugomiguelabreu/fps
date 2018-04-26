package Offline.LocalTorrent;

import Handlers.TorrentListenerInitializer;
import com.turn.ttorrent.common.Torrent;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.ArrayList;

public class TorrentListener extends Thread{

    private int port;
    private ChannelFuture cf;
    private EventLoopGroup workerGroup;
    private EventLoopGroup bossGroup;
    private String ipv4;
    private ArrayList<Torrent> available;

    public TorrentListener(String ipv4, int port, ArrayList<Torrent> availableParam){
        this.available = availableParam;
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
                    .childHandler(new TorrentListenerInitializer(this.available));
            cf = b.bind(ipv4,port).sync();
            System.out.println("Torrent Listener initiated");
            cf.channel().closeFuture().sync();
            //Wait for channel to close
            System.out.println("Server shutting down.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
