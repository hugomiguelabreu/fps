import Handlers.TorrentServerInitializer;
import com.turn.ttorrent.common.Torrent;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.List;

public class MainServer extends Thread{
    private int port;
    private ChannelFuture cf;
    private EventLoopGroup workerGroup;
    private EventLoopGroup bossGroup;
    private List<Torrent> trackedTorrents;

    public MainServer(int portParam, List<Torrent> trackedTorrentsParam){
        port = portParam;
        trackedTorrents = trackedTorrentsParam;
        //Acceptor
        bossGroup = new NioEventLoopGroup(1);
        //Workers
        workerGroup = new NioEventLoopGroup();
    }

    public void run(){
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new TorrentServerInitializer(trackedTorrents));

            cf = b.bind(port).sync();
            System.out.println("Server initiated.");
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
