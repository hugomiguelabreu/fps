package Core;

import Handlers.TorrentServerInitializer;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.HashMap;

public class MainServer extends Thread{
    private int port;
    private ChannelFuture cf;
    private EventLoopGroup workerGroup;
    private EventLoopGroup bossGroup;
    private Tracker tck;
    private HashMap<String, ServerClient> clients;

    public MainServer(int portParam, Tracker tckParam, HashMap<String, ServerClient> clientsParam){
        this.port = portParam;
        this.tck = tckParam;
        this.clients = clientsParam;
        //Acceptor
        this.bossGroup = new NioEventLoopGroup(1);
        //Workers
        this.workerGroup = new NioEventLoopGroup();
    }

    public void run(){
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new TorrentServerInitializer(tck, clients));

            cf = b.bind(port).sync();
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

    public void shutdown(){
        //Close server channel;
        cf.channel().close();
    }
}
