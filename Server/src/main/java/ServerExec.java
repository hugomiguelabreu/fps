import Handlers.TorrentServerInitializer;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;
import com.turn.ttorrent.tracker.TrackedTorrent;
import com.turn.ttorrent.tracker.Tracker;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ServerExec {

    public static void main(String[] args) throws IOException, InterruptedException {
        //Starts tracker;
        Tracker tck = new Tracker(new InetSocketAddress(6969));
        System.out.println("Tracker initiated");
        MainServer ms = new MainServer(5000, tck);
        //Starts server;
        ms.start();

        Scanner reader = new Scanner(System.in);
        String line;
        while (!(line = reader.nextLine()).equals("quit")){
            for (TrackedTorrent t:tck.getTrackedTorrents()) {
                System.out.println(t.getName());
                System.out.println(t.getCreatedBy());
                System.out.println("---------------------------");
            }
        }
        ms.shutdown();
    }

}
