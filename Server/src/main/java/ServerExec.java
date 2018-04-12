import Handlers.TorrentServerInitializer;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerExec {

    static final int PORT = Integer.parseInt(System.getProperty("port", "5000"));
    static List<Torrent> trackedTorrents;

    public ServerExec(){
        trackedTorrents = new ArrayList<>();
    }

    public static void main(String[] args) {
        trackedTorrents = new ArrayList<>();
        //Acceptor
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //Workers
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new TorrentServerInitializer(trackedTorrents));

            b.bind(PORT).sync().channel().closeFuture();
            System.out.println("Server initiated");
            Tracker tck = new Tracker(new InetSocketAddress(6969));
            System.out.println("Tracker initiated");

            while (true){
                if(!trackedTorrents.isEmpty()){
                    for (Torrent t:trackedTorrents) {
                        System.out.println(t.getName());
                        System.out.println(t.getCreatedBy());
                        System.out.println("---------------------------");
                    }
                }
                Thread.sleep(5000);
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
